package com.example.psyaihealer.alert;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService service;

    public AlertController(AlertService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasRole('COUNSELOR') or hasRole('ADMIN')")
    public ResponseEntity<List<Alert>> list() {
        return ResponseEntity.ok(service.pending());
    }

    @PostMapping("/resolve/{id}")
    @PreAuthorize("hasRole('COUNSELOR') or hasRole('ADMIN')")
    public ResponseEntity<Alert> resolve(@PathVariable Long id) {
        return ResponseEntity.ok(service.resolve(id));
    }
}
