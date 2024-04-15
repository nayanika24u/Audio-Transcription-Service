package com.audiotranscriptionservice.service;

import java.time.LocalDateTime;
import java.util.Map;

public class TranscriptResult {
    /**
     * Type: TranscriptResult {
     *     transcriptText=string
     *     chunkStatuses={ audioPath: chunkStatus }
     *     jobStatus=string,
     * 		completedTime=DateTime/ISOString/etc
     * }
     * */
    private String transcriptText;
    private Map<String, String> chunkStatuses; // Map of audioPath to chunkStatus
    private String jobStatus;
    private LocalDateTime completedTime;

    public String getTranscriptText() {
        return transcriptText;
    }

    public void setTranscriptText(String transcriptText) {
        this.transcriptText = transcriptText;
    }

    public Map<String, String> getChunkStatuses() {
        return chunkStatuses;
    }

    public void setChunkStatuses(Map<String, String> chunkStatuses) {
        this.chunkStatuses = chunkStatuses;
    }

    public String getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }

    public LocalDateTime getCompletedTime() {
        return completedTime;
    }

    public void setCompletedTime(LocalDateTime completedTime) {
        this.completedTime = completedTime;
    }
}
