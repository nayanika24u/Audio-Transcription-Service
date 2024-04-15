package com.audiotranscriptionservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TranscriptionController {
    @Autowired
    private TranscriptionService transcriptionService;

    @PostMapping("/transcribe")
    public ResponseEntity<String> transcribe(@RequestParam List<String> audioPaths, @RequestParam String userId) throws IOException {
        Long jobId = transcriptionService.createJob(audioPaths, userId);
        return ResponseEntity.ok(String.valueOf(jobId));
    }

    @GetMapping("/transcript/{jobId}")
    public ResponseEntity<TranscriptResult> getTranscript(@PathVariable Long jobId) {
        try {
            TranscriptResult result = transcriptionService.getTranscript(jobId);
            return ResponseEntity.ok(result);
        } catch (IOException | ClassNotFoundException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/transcript/search")
    public ResponseEntity<TranscriptResult[]> searchTranscripts(@RequestParam String jobStatus, @RequestParam String userId) {
        try {
            TranscriptResult[] results = transcriptionService.searchTranscripts(jobStatus, userId);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
