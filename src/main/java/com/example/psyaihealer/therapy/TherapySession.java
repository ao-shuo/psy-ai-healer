package com.example.psyaihealer.therapy;

import com.example.psyaihealer.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "therapy_sessions")
public class TherapySession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User user;

    private String topic;

    private LocalDateTime createdAt = LocalDateTime.now();

    public TherapySession() {
    }

    public TherapySession(User user, String topic) {
        this.user = user;
        this.topic = topic;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
