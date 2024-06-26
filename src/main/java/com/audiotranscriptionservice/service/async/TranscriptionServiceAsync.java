package com.audiotranscriptionservice.service.async;

import com.audiotranscriptionservice.service.*;
import com.audiotranscriptionservice.service.internal.GetTranscriptRequest;
import com.audiotranscriptionservice.service.internal.GetTranscriptResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class TranscriptionServiceAsync {
    private AtomicLong jobIdCounter = new AtomicLong();
    private final Logger logger = LoggerFactory.getLogger(TranscriptionServiceAsync.class);
    ExecutorService failedJobThreadPool = Executors.newFixedThreadPool(2);
    @Autowired
    private ATSCoreService atsCoreService;
    private JobManager jobManager;
    private ConcurrentHashMap<String, ConcurrentHashMap<String, String>> userToJobInfoMap;

    // only for running unit test case
    public TranscriptionServiceAsync(ATSCoreService atsCoreService) {
        this.atsCoreService = atsCoreService;
        this.jobManager = new JobManager(); // inits the Write ahead log from disk file
        this.userToJobInfoMap = new ConcurrentHashMap<>();
    }

    @Async("asyncExecutor")
    public CompletableFuture<Long> createJob(List<String> audioPaths, String userId) throws IOException {
        if (userId == null || audioPaths == null || audioPaths.isEmpty()) {
            throw new IllegalArgumentException("Check transcript job arguments. Found userId: "+ userId + " audioPaths: "+audioPaths);
        }
        return CompletableFuture.supplyAsync(() -> {
            Long jobId = jobIdCounter.incrementAndGet();
            Job job = new Job(jobId, userId, Job.Status.PENDING.name(), LocalDateTime.now());
            for (String path : audioPaths) {
                job.getChunks().add(new AudioChunk(path, Job.Status.PENDING.name()));
            }
            try {
                StorageUtil.saveJob(job); // Serialize job to disk
            } catch (IOException e) {
                // log error
                return null; // job info couldn't be saved. Try again.
            }
            // update WAL with successfully created job with userId
            jobManager.logJobCreation(jobId, userId); // successful case only
            try {
                processJob(jobId); // async processing of job is triggered
            } catch (IllegalStateException e) {
                // log message but don't fail the job creation in case cancellation request comes concurrently.
                // it's an edge case; all threads will be writing the job info to separate files. Only, one file will be
                // updated concurrently ie job-log.txt.
            }
            return jobId;
        });
    }

    @Async("asyncExecutor")
    public CompletableFuture<TranscriptResult> getTranscript(Long jobId) throws IOException, ClassNotFoundException {
        if (!jobManager.checkJobExists(jobId)) {
            throw new IllegalArgumentException("Job Id not found. Possible input error?");
        }
        return processJob(jobId).thenApply(job -> {
            // prepare response
            return prepareTranscriptResult(job);
        });
    }

    @Async("asyncExecutor")
    public CompletableFuture<Job> processJob(Long jobId) {
        // call the HTTP client in ATSCoreService to fetch the transcripts for a job and store them in their respective disk files
        // fetch userId info for the jobId
        String userId = jobManager.getUserId(jobId);
        if (userId == null) {
            throw new IllegalArgumentException("No userId found for the job. Will not continue further as it result in file path failure.");
        }
        return CompletableFuture.supplyAsync(() -> {
            Job job = null;
            try {
                job = StorageUtil.loadJob(jobId, userId);
                if (job.getStatus().equals(Job.Status.COMPLETED)) {
                    return job; // check if job has been completed then return from here
                } else {
                    if (job.getStatus().equals(Job.Status.CANCELLED)) {
                        throw new IllegalStateException("JobId " + jobId + " is cancelled. Not processing further.");
                    }
                }
                List<AudioChunk> chunks = job.getChunks();
                int allCompleted = 0;
                for (AudioChunk chunk : chunks) {
                    String audioFilePath = chunk.getAudioPath();
                    GetTranscriptResponse response = atsCoreService.getTranscript(new GetTranscriptRequest(audioFilePath));
                    if (response == null || !response.getFilePath().equalsIgnoreCase(audioFilePath) || response.getTranscript() == null) {
                        // log error and mark chunk as failed
                        chunk.setStatus(Job.Status.FAILED.name());
                        // do not fail the job; use a retry logic to fetch only failed chunks and save them to disk
                        // check if the failure is due to file not existing
                    } else {
                        chunk.setTranscript(response.getTranscript());
                        chunk.setStatus(Job.Status.COMPLETED.name());
                        allCompleted++;
                    }
                }
                if (allCompleted == job.getChunks().size()) {
                    job.setStatus(Job.Status.COMPLETED.name());
                } else {
                    job.setStatus(Job.Status.FAILED.name());
                }
                StorageUtil.saveJob(job); // persist the updated job to disk once all chunks are processed so that progress is not lost
                // store job status to another file for resume/repair process
                StorageUtil.saveJobStatus(job.getJobId(), userId, job.getStatus());
            } catch (IOException | ClassNotFoundException e) {
                // mark job as failed; where to store this status? separate file. Can be map too which is flushed from time to time to disk for persistence
                logger.error("Error: JobId " + jobId + " file failed to open.", e);
                captureFailedJobStatus(userId, jobId, Job.Status.FAILED.name());
            }
            return job; // failed; no job info retrieved
        });
    }

//    @Async("asyncExecutor")
//    public CompletableFuture<TranscriptResult[]> searchTranscripts(String jobStatus, String userId) {
//        return CompletableFuture.supplyAsync(() -> {TranscriptResult[0];}); // delegate on jvm to allocate the size of this array
//    }

    /**
     * Scheduled repair service to do retries.
     * */
    private void maintainJobStatus() {
        // writes and reads a separate file

    }

    private void captureFailedJobStatus(String userId, Long jobId, String jobStatus) {
        // can this be done via another thread to reduce response time
        failedJobThreadPool.submit(()-> {
                try {
                    StorageUtil.saveJobStatus(jobId, userId, jobStatus);
                } catch (IOException e1) {
                    logger.info("Error in capturing failed job status", e1); // it's okay to ignore it if it fails
                }
            }); // don't wait for this result. Best effort
    }

    public CompletableFuture<TranscriptResult[]> searchTranscripts(String userId, String jobStatus) {
        if (userId == null || userId.isEmpty()) {
            CompletableFuture<TranscriptResult[]> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new IllegalArgumentException("UserId found null or empty. Cannot access job information for it."));
            return failedFuture;
        }
        final String finalJobStatus = jobStatus.isEmpty()? Job.Status.COMPLETED.name() : jobStatus.toUpperCase();
        // check userToJobInfoMap first else load the file into this map

        return CompletableFuture.supplyAsync(() -> {
            List<String> completedJobIds = new ArrayList<>();
            // entries can be stale but we can save some repetitive request computations
            if (!userToJobInfoMap.containsKey(userId)) {
                // load from file into this map
                try {
                    populateUserToJobInfoMap(userId);
                } catch (IOException e) {
                    logger.info("Failed to populate the userToJobInfo in-memory map. ", e);
                    // best effort loading of file into application cache
                }
            }
            completedJobIds = userToJobInfoMap.get(userId).entrySet().stream()
                    .filter(e -> e.getValue().equals(finalJobStatus))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            if (completedJobIds.isEmpty()) {
                return new TranscriptResult[0];
            }

            // prepare the result by fetching content from the respective jobId files
            TranscriptResult[] result = new TranscriptResult[completedJobIds.size()];

            // read file, fetch transcriptResult field and add to this array
            for (int i = 0; i < completedJobIds.size(); i++) {
                String jobId = completedJobIds.get(i);
                try {
                    Job currentJob = StorageUtil.loadJob(Long.parseLong(jobId), userId);
                    result[i] = prepareTranscriptResult(currentJob);
                } catch (IOException | ClassNotFoundException e) {
                    // don't fail; try best effort
                    result[i] = new TranscriptResult(); // place holder info
                }
            }
            return result;
        });
    }

        private TranscriptResult prepareTranscriptResult(Job job) {
            TranscriptResult result = new TranscriptResult();
            result.setJobStatus(job.getStatus());
            result.setCompletedTime(LocalDateTime.now());
            HashMap<String, String> statuses = new HashMap<>();
            for (AudioChunk chunk : job.getChunks()) {
                statuses.put(chunk.getAudioPath(), chunk.getStatus());
            }
            result.setChunkStatuses(statuses);

            // Aggregate transcripts
            result.setTranscriptText(job.getChunks().stream()
                    .map(AudioChunk::getTranscript)
                    .collect(Collectors.joining(" ")));
            return result;
        }

    private void populateUserToJobInfoMap(String userId) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(StorageUtil.getFilePath(userId+"/job-status.txt")));
        ConcurrentHashMap<String, String> jobStatusMap = new ConcurrentHashMap<>();

        for (String line : lines) {
            String[] parts = line.split(" "); // Assuming space separates jobId and status
            if (parts.length == 2) { // Ensure there are exactly two parts
                String jobId = parts[0];
                String status = parts[1];
                jobStatusMap.put(jobId, status);
            }
        }

        // Populate the map with the loaded data
        userToJobInfoMap.put(userId, jobStatusMap);
    }
}

