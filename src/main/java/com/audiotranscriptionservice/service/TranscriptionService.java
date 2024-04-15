//package com.audiotranscriptionservice.service;
//
//import com.audiotranscriptionservice.service.internal.GetTranscriptRequest;
//import com.audiotranscriptionservice.service.internal.GetTranscriptResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.concurrent.atomic.AtomicLong;
//import java.util.stream.Collectors;
//

// Commented because this is too slow. Refer to the async package.


//@Service
//public class TranscriptionService {
//    private AtomicLong jobIdCounter = new AtomicLong();
//    @Autowired
//    ATSCoreService atsCoreService;
//
//    // only for running unit test case
//    public TranscriptionService(ATSCoreService atsCoreService) {
//        this.atsCoreService = atsCoreService;
//    }
//
//    public Long createJob(List<String> audioPaths, String userId) throws IOException {
//        Long jobId = jobIdCounter.incrementAndGet();
//        Job job = new Job(jobId, userId, Job.Status.PENDING.name(), LocalDateTime.now());
//        for (String path : audioPaths) {
//            job.getChunks().add(new AudioChunk(path, Job.Status.PENDING.name()));
//        }
//        StorageUtil.saveJob(job); // Serialize job to disk
//        return jobId;
//    }
//
//    public TranscriptResult getTranscript(Long jobId) throws IOException, ClassNotFoundException {
//
//        // process job and then fetch processed info in disk files
//        processJob(jobId);
//        // prepare response
//        Job job = StorageUtil.loadJob(jobId, userId);
//        TranscriptResult result = new TranscriptResult();
//        result.setJobStatus(job.getStatus());
//        result.setCompletedTime(job.getCreationTime());
//        HashMap<String, String> statuses = new HashMap<>();
//        for (AudioChunk chunk : job.getChunks()) {
//            statuses.put(chunk.getAudioPath(), chunk.getStatus());
//        }
//        result.setChunkStatuses(statuses);
//
//        // Aggregate transcripts
//        result.setTranscriptText(job.getChunks().stream()
//                .map(AudioChunk::getTranscript)
//                .collect(Collectors.joining(" ")));
//        return result;
//    }
//
//    public void processJob(Long jobId) {
//        // call the HTTP client in ATSCoreService to fetch the transcripts for a job and store them in their respective disk files
//        try {
//            Job job = StorageUtil.loadJob(jobId);
//            List<AudioChunk> chunks = job.getChunks();
//            int allCompleted = 0;
//            for (AudioChunk chunk : chunks) {
//                String audioFilePath = chunk.getAudioPath();
//                GetTranscriptResponse response = atsCoreService.getTranscript(new GetTranscriptRequest(audioFilePath));
//                if (response == null || !response.getFilePath().equalsIgnoreCase(audioFilePath) || response.getTranscript() == null) {
//                    // log error and mark chunk as failed
//                    chunk.setStatus(Job.Status.FAILED.name());
//                    // do not fail the job; use a retry logic to fetch only failed chunks and save them to disk
//                    // check if the failure is due to file not existing
//                } else {
//                    chunk.setTranscript(response.getTranscript());
//                    chunk.setStatus(Job.Status.COMPLETED.name());
//                    allCompleted++;
//                }
//            }
//            if (allCompleted == job.getChunks().size()) {
//                job.setStatus(Job.Status.COMPLETED.name());
//            } else {
//                job.setStatus(Job.Status.FAILED.name());
//            }
//            StorageUtil.saveJob(job); // persist the updated job to disk once all chunks are processed so that progress is not lost
//        } catch(IOException | ClassNotFoundException e) {
//            // mark job as failed; where to store this status? separate file. Can be map too which is flushed from time to time to disk for persistence
//            System.out.println("Error: JobId "+ jobId+" file failed to open.");
//        }
//
//    }
//
//    public TranscriptResult[] searchTranscripts(String jobStatus, String userId) {
//        return new TranscriptResult[0]; // delegate on jvm to allocate the size of this array
//    }
//
//    private void maintainJobStatus() {
//        // writes and reads a separate file
//
//    }
//}
