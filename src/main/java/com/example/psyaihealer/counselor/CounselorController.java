package com.example.psyaihealer.counselor;

import com.example.psyaihealer.alert.Alert;
import com.example.psyaihealer.alert.AlertService;
import com.example.psyaihealer.assessment.Phq9Assessment;
import com.example.psyaihealer.assessment.Phq9AssessmentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/counselor")
@PreAuthorize("hasRole('THERAPIST') or hasRole('ADMIN')")
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

    @GetMapping("/assessments/latest")
    public ResponseEntity<List<Phq9Assessment>> latestAssessments() {
        // For demo simplicity, return all; real-world would paginate/anonymize more.
        return ResponseEntity.ok(phq9Repo.findAll());
    }
}
