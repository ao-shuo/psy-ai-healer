package com.example.psyaihealer.alert;

import com.example.psyaihealer.user.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlertService {
    private final AlertRepository repository;

    public AlertService(AlertRepository repository) {
        this.repository = repository;
    }

    public Alert raiseRiskAlert(User user, String message, Integer score, String source) {
        Alert alert = new Alert(user, "RISK", source, message, score);
        return repository.save(alert);
    }

    public List<Alert> pending() {
        return repository.findByStatusOrderByCreatedAtDesc(Alert.Status.PENDING);
    }

    public Alert resolve(Long id) {
        Alert alert = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("告警不存在"));
        alert.setStatus(Alert.Status.RESOLVED);
        return repository.save(alert);
    }
}
