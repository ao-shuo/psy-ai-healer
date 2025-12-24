package com.example.psyaihealer.therapy;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TherapyMessageRepository extends JpaRepository<TherapyMessage, Long> {
    List<TherapyMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);
}
