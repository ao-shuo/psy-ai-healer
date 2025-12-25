package com.example.psyaihealer.growth;

import com.example.psyaihealer.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "growth_logs")
public class GrowthLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 32)
    private String mood;

    @Column
    private Integer moodScore; // 0-10

    @Column(length = 2000)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public GrowthLog() {}

    public GrowthLog(User user, String mood, Integer moodScore, String content) {
        this.user = user;
        this.mood = mood;
        this.moodScore = moodScore;
        this.content = content;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getMood() { return mood; }
    public Integer getMoodScore() { return moodScore; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
