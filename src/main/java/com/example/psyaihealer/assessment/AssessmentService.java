package com.example.psyaihealer.assessment;

import com.example.psyaihealer.dto.AssessmentRequest;
import com.example.psyaihealer.dto.AssessmentResponse;
import com.example.psyaihealer.user.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssessmentService {

    private final Phq9AssessmentRepository repository;

    public AssessmentService(Phq9AssessmentRepository repository) {
        this.repository = repository;
    }

    public AssessmentResponse scorePhq9(User user, AssessmentRequest request) {
        List<Integer> answers = request.getAnswers();
        if (answers == null || answers.size() != 9) {
            throw new IllegalArgumentException("PHQ-9需要9个答案");
        }
        int score = answers.stream().mapToInt(Integer::intValue).sum();
        String severity = severity(score);
        String answerString = answers.stream().map(String::valueOf).collect(Collectors.joining(","));
        repository.save(new Phq9Assessment(user, score, severity, answerString));
        return new AssessmentResponse(score, severity);
    }

    public List<Phq9Assessment> history(User user) {
        return repository.findByUser(user);
    }

    private String severity(int score) {
        if (score <= 4) return "最轻微";
        if (score <= 9) return "轻度";
        if (score <= 14) return "中度";
        if (score <= 19) return "中重度";
        return "重度";
    }
}
