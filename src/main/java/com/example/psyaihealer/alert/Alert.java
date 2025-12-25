package com.example.psyaihealer.alert;

import com.example.psyaihealer.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 64)
    private String type; // RISK

    @Column(length = 64)
    private String source; // PHQ9 / THERAPY

    @Column(length = 1024)
    private String message;

    @Column
    private Integer score; // optional risk score

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private Status status = Status.PENDING;

    public enum Status { PENDING, RESOLVED }

    public Alert() {}

    public Alert(User user, String type, String source, String message, Integer score) {
        this.user = user;
        this.type = type;
        this.source = source;
        this.message = message;
        this.score = score;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getType() { return type; }
    public String getSource() { return source; }
    public String getMessage() { return message; }
    public Integer getScore() { return score; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
