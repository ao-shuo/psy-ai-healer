package com.example.psyaihealer.dto;

import java.util.List;

public class AssessmentRequest {
    private List<Integer> answers;

    public List<Integer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Integer> answers) {
        this.answers = answers;
    }
}
