package com.example.psyaihealer.therapy;

import com.example.psyaihealer.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TherapySessionRepository extends JpaRepository<TherapySession, Long> {
    List<TherapySession> findByUser(User user);
}
