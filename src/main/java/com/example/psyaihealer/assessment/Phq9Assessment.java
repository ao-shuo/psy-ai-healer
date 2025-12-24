package com.example.psyaihealer.assessment;

import com.example.psyaihealer.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "phq9_assessments")
public class Phq9Assessment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User user;

    private int score;

    @Column(length = 200)
    private String severity;

    @Column(length = 120)
    private String answers;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Phq9Assessment() {
    }

    public Phq9Assessment(User user, int score, String severity, String answers) {
        this.user = user;
        this.score = score;
        this.severity = severity;
        this.answers = answers;
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

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getAnswers() {
        return answers;
    }

    public void setAnswers(String answers) {
        this.answers = answers;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
