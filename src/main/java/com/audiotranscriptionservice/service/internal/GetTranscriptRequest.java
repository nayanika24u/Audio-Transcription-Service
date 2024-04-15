package com.audiotranscriptionservice.service.internal;

public class GetTranscriptRequest {
    private String audioFilePath;

    public GetTranscriptRequest(String audioFilePath) {
        this.audioFilePath = audioFilePath;
    }

    public String getAudioFilePath() {
        return audioFilePath;
    }

    public void setAudioFilePath(String audioFilePath) {
        this.audioFilePath = audioFilePath;
    }
}
