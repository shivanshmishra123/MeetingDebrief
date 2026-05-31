package com.example.demo.dto;

public class QueryRequestDTO {
    private String question;
    private String meetingId;

    public QueryRequestDTO() {}

    public QueryRequestDTO(String question, String meetingId) {
        this.question = question;
        this.meetingId = meetingId;
    }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getMeetingId() { return meetingId; }
    public void setMeetingId(String meetingId) { this.meetingId = meetingId; }
}