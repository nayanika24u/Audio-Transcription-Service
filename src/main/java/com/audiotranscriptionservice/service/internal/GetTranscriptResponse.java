package com.audiotranscriptionservice.service.internal;

public class GetTranscriptResponse {
    private String filePath;
    private String transcript;

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public String getTranscript() {
        return transcript;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
