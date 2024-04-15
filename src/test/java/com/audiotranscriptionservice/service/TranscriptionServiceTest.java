package com.audiotranscriptionservice.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.*;

public class TranscriptionServiceTest {

    TranscriptionService transcriptionService = new TranscriptionService(new ATSCoreService());
    HashSet<Long> newJobs = new HashSet<>();

    @Test
    public void testCreateJob() {
        List<String> audioPaths = Arrays.asList("audio-file-7.wav", "audio-file-1.wav");
        Long jobId = 0l;
        try {
            jobId = transcriptionService.createJob(audioPaths, "userId-1");
        } catch (IOException e) {
          // Assert.fail()
        }
        org.junit.jupiter.api.Assertions.assertTrue(jobId > 0, "JobId should be non zero for a successfully created new job.");

        // process job
        transcriptionService.processJob(jobId);

    }

    @Test
    public void testGetTranscript() {
        Long jobId = 1l;
        // file paths
        List<String> audioFilePaths = Arrays.asList("audio-file-7.wav", "audio-file-1.wav");
        try {
            TranscriptResult result = transcriptionService.getTranscript(jobId);
            // assert result
            Assertions.assertNotNull(result, "JobId:" + jobId + ";TranscribeResult should not be null");
            Assertions.assertNotNull(result.getChunkStatuses(),"JobId:" + jobId +  ";chunk statuses found null");
            Assertions.assertNotNull(result.getJobStatus(), "JobId:" + jobId + ";job status found null");
            Assertions.assertNotNull(result.getTranscriptText(), "JobId:" + jobId + ";transcript text found null");
            Map<String, String> chunkStatuses = result.getChunkStatuses();
            for (String audioFile: chunkStatuses.keySet()) {
                Assertions.assertTrue(audioFilePaths.contains(audioFile));
                // all chunks should be completed; failures can be recorded in a disk file/map
                Assertions.assertTrue(chunkStatuses.get(audioFile).equals(Job.Status.COMPLETED.name()), audioFile + " status marked "+ chunkStatuses.get(audioFile));
            }
            Assertions.assertTrue(result.getJobStatus().equals(Job.Status.COMPLETED.name()));
            // MD5 check for file content integrity
        } catch (Exception e) {
            // assert.fail
            Assertions.fail("Failure in transcribing for job "+ jobId, e);
        }
    }
}
