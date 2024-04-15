package com.audiotranscriptionservice.service;

import java.io.Serializable;

public class AudioChunk implements Serializable {
    private String audioPath;
    private String status; // pending, processing, completed, failed
    private String transcript;

    public AudioChunk(String audioPath, String status, String transcript) {
        this.audioPath = audioPath;
        this.status = status;
        this.transcript = transcript;
    }

    public AudioChunk(String audioPath, String status) {
        this.audioPath = audioPath;
        this.status = status;
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
