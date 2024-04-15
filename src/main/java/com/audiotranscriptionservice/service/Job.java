package com.audiotranscriptionservice.service;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Job implements Serializable {
    private Long jobId;
    private String userId;
    private List<AudioChunk> chunks;
    private String status; // pending, processing, completed, failed
    private transient String filePath; // Not serialized, only used at runtime
    private LocalDateTime creationTime;

    public enum Status {
        PENDING, PROCESSING, COMPLETED, FAILED
    };

    public Job(Long jobId, String userId, String status, LocalDateTime creationTime) {
        this.jobId = jobId;
        this.userId = userId;
        this.status = status;
        this.creationTime = creationTime;
        this.chunks = new ArrayList<>();
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public List<AudioChunk> getChunks() {
        return chunks;
    }

    public void setChunks(List<AudioChunk> chunks) {
        this.chunks = chunks;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }
}

