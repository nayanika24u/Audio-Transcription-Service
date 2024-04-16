package com.audiotranscriptionservice.service.async;

import com.audiotranscriptionservice.service.Job;
import com.audiotranscriptionservice.service.TranscriptResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api")
public class TranscriptionControllerAsync {
    @Autowired
    private TranscriptionServiceAsync transcriptionService;

    @PostMapping("/transcribe")
    public ResponseEntity<String> transcribe(@RequestParam List<String> audioPaths, @RequestParam String userId) {
        try {
            // Start the asynchronous operation and block until the result is available
            Long jobId = transcriptionService.createJob(audioPaths, userId).get();  // Block and get the result
            return ResponseEntity.ok(String.valueOf(jobId));
        } catch(IllegalArgumentException e) {
            return ResponseEntity.status(org.apache.http.HttpStatus.SC_NOT_FOUND).body("Check request arguments. UserId: "+ userId);
        } catch (InterruptedException | IOException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Request interrupted.");
        }
    }

    @GetMapping("/transcript/{jobId}")
    public ResponseEntity<TranscriptResult> getTranscript(@PathVariable Long jobId) {
        try {
            TranscriptResult result = transcriptionService.getTranscript(jobId).get();  // Block and get the result
            return ResponseEntity.ok(result);
        } catch (InterruptedException | IOException | ClassNotFoundException | ExecutionException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header("Error", e.getMessage()).build();
        }
    }

    @GetMapping("/transcript/search")
    public ResponseEntity<TranscriptResult[]> searchTranscripts(@RequestParam String jobStatus, @RequestParam String userId) {
        try {
            TranscriptResult[] results = transcriptionService.searchTranscripts(jobStatus, userId).get();  // Block and get the result
            return ResponseEntity.ok(results);
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // cancel job endpoint to stop erroneous job triggers; update the job status to cancelled in the job's info file
    // edge case: may result in concurrent access by 2 threads updating the status in the file as the StorageUtil class
    // doesn't have synchronized methods. Data corruption chance.


}
