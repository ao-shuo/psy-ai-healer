package com.example.psyaihealer.assessment;

import com.example.psyaihealer.dto.AssessmentRequest;
import com.example.psyaihealer.dto.AssessmentResponse;
import com.example.psyaihealer.user.User;
import com.example.psyaihealer.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/assessments")
public class AssessmentController {

    private final AssessmentService assessmentService;
    private final UserRepository userRepository;

    public AssessmentController(AssessmentService assessmentService, UserRepository userRepository) {
        this.assessmentService = assessmentService;
        this.userRepository = userRepository;
    }

    @PostMapping("/phq9")
    public ResponseEntity<AssessmentResponse> phq9(@AuthenticationPrincipal UserDetails principal,
                                                   @RequestBody AssessmentRequest request) {
        User user = userRepository.findByUsername(principal.getUsername()).orElseThrow();
        return ResponseEntity.ok(assessmentService.scorePhq9(user, request));
    }

    @GetMapping("/phq9")
    public ResponseEntity<List<Phq9Assessment>> history(@AuthenticationPrincipal UserDetails principal) {
        User user = userRepository.findByUsername(principal.getUsername()).orElseThrow();
        return ResponseEntity.ok(assessmentService.history(user));
    }
}
