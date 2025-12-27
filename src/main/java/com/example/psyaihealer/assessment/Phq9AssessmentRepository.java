package com.example.psyaihealer.assessment;

import com.example.psyaihealer.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface Phq9AssessmentRepository extends JpaRepository<Phq9Assessment, Long> {
    List<Phq9Assessment> findByUser(User user);

    List<Phq9Assessment> findAllByOrderByCreatedAtDesc();
}
