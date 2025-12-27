package com.example.psyaihealer.counselor;

import com.example.psyaihealer.alert.Alert;
import com.example.psyaihealer.alert.AlertService;
import com.example.psyaihealer.assessment.Phq9Assessment;
import com.example.psyaihealer.assessment.Phq9AssessmentRepository;
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

    public CounselorController(AlertService alertService, Phq9AssessmentRepository phq9Repo) {
        this.alertService = alertService;
        this.phq9Repo = phq9Repo;
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
}
