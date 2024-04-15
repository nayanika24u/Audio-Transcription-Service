package com.audiotranscriptionservice.service.async;

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
        } catch (InterruptedException | IOException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Request interrupted.");
        }
    }

    @GetMapping("/transcript/{jobId}")
    public ResponseEntity<TranscriptResult> getTranscript(@PathVariable Long jobId) {
        try {
            TranscriptResult result = transcriptionService.getTranscript(jobId).get();  // Block and get the result
            return ResponseEntity.ok(result);
        } catch (InterruptedException | IOException | ClassNotFoundException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

//    @GetMapping("/transcript/search")
//    public ResponseEntity<TranscriptResult[]> searchTranscripts(@RequestParam String jobStatus, @RequestParam String userId) {
//        try {
//            TranscriptResult[] results = transcriptionService.searchTranscripts(jobStatus, userId).get();  // Block and get the result
//            return ResponseEntity.ok(results);
//        } catch (InterruptedException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
}
