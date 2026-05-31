package com.example.demo.dto;

public class QueryResponseDTO {
    private String question;
    private String answer;
    private int sources_used;

    public QueryResponseDTO() {}

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public int getSources_used() { return sources_used; }
    public void setSources_used(int sources_used) { this.sources_used = sources_used; }
}