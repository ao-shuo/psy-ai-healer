package com.example.psyaihealer.counselor;

import com.example.psyaihealer.alert.Alert;
import com.example.psyaihealer.alert.AlertService;
import com.example.psyaihealer.assessment.Phq9Assessment;
import com.example.psyaihealer.assessment.Phq9AssessmentRepository;
import com.example.psyaihealer.profile.UserProfile;
import com.example.psyaihealer.profile.UserProfileService;
import com.example.psyaihealer.profile.UserProfileViewDto;
import com.example.psyaihealer.user.User;
import com.example.psyaihealer.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/counselor")
@PreAuthorize("hasRole('COUNSELOR') or hasRole('ADMIN')")
public class CounselorController {

    private final AlertService alertService;
    private final Phq9AssessmentRepository phq9Repo;
    private final UserRepository userRepository;
    private final UserProfileService profileService;

    public CounselorController(AlertService alertService,
                              Phq9AssessmentRepository phq9Repo,
                              UserRepository userRepository,
                              UserProfileService profileService) {
        this.alertService = alertService;
        this.phq9Repo = phq9Repo;
        this.userRepository = userRepository;
        this.profileService = profileService;
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<Alert>> alerts() {
        return ResponseEntity.ok(alertService.pending());
    }

    @GetMapping("/alerts/{id}")
    public ResponseEntity<Alert> alert(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.getOrThrow(id));
    }

    @PatchMapping("/alerts/{id}/resolve")
    public ResponseEntity<Alert> resolve(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.resolve(id));
    }

    @GetMapping("/assessments")
    public ResponseEntity<List<Phq9Assessment>> assessments(@RequestParam(name = "limit", required = false) Integer limit) {
        List<Phq9Assessment> all = phq9Repo.findAllByOrderByCreatedAtDesc();
        if (limit == null || limit <= 0 || limit >= all.size()) {
            return ResponseEntity.ok(all);
        }
        return ResponseEntity.ok(all.subList(0, limit));
    }

    @GetMapping("/assessments/{id}")
    public ResponseEntity<Phq9Assessment> assessment(@PathVariable Long id) {
        return ResponseEntity.ok(phq9Repo.findById(id).orElseThrow(() -> new IllegalArgumentException("测评不存在")));
    }

    @GetMapping("/users/{id}/profile")
    public ResponseEntity<UserProfileViewDto> userProfile(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        UserProfile profile = profileService.getOrCreate(user);
        return ResponseEntity.ok(UserProfileViewDto.of(user, profile));
    }
}
