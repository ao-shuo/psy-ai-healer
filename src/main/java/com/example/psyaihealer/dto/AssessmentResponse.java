package com.example.psyaihealer.dto;

public class AssessmentResponse {
    private int score;
    private String severity;

    public AssessmentResponse() {
    }

    public AssessmentResponse(int score, String severity) {
        this.score = score;
        this.severity = severity;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }
}
