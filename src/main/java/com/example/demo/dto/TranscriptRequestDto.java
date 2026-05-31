package com.example.demo.dto;

public class TranscriptRequestDto {
    private String transcript;

    public TranscriptRequestDto() {}

    public TranscriptRequestDto(String transcript) {
        this.transcript = transcript;
    }

    public String getTranscript() {
        return transcript;
    }
}