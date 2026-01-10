package com.example.psyaihealer.assessment;

import com.example.psyaihealer.alert.AlertService;
import com.example.psyaihealer.dto.AssessmentRequest;
import com.example.psyaihealer.dto.AssessmentResponse;
import com.example.psyaihealer.profile.UserProfileService;
import com.example.psyaihealer.user.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssessmentService {

    private final Phq9AssessmentRepository repository;
    private final AlertService alertService;
    private final UserProfileService profileService;

    public AssessmentService(Phq9AssessmentRepository repository,
                             AlertService alertService,
                             UserProfileService profileService) {
        this.repository = repository;
        this.alertService = alertService;
        this.profileService = profileService;
    }

    public AssessmentResponse scorePhq9(User user, AssessmentRequest request) {
        List<Integer> answers = request.getAnswers();
        if (answers == null || answers.size() != 9) {
            throw new IllegalArgumentException("PHQ-9需要9个答案");
        }
        if (answers.stream().anyMatch(ans -> ans < 0 || ans > 3)) {
            throw new IllegalArgumentException("PHQ-9答案需在0-3之间");
        }
        int score = answers.stream().mapToInt(Integer::intValue).sum();
        String severity = severity(score);
        String answerString = answers.stream().map(String::valueOf).collect(Collectors.joining(","));
        repository.save(new Phq9Assessment(user, score, severity, answerString));

        // Keep the user profile up-to-date for personalized therapy replies.
        profileService.updateFromPhq9(user, score, severity);

        if (score >= 15) {
            alertService.raiseRiskAlert(user, "PHQ-9得分偏高（建议人工跟进）", score, "PHQ9");
        }
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
