package com.example.psyaihealer.alert;

import com.example.psyaihealer.alert.Alert.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByStatusOrderByCreatedAtDesc(Status status);
}
