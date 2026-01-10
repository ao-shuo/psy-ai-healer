package com.example.psyaihealer.profile;

import com.example.psyaihealer.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    /** Latest PHQ-9 snapshot */
    private Integer lastPhq9Score;

    @Column(length = 64)
    private String lastPhq9Severity;

    private LocalDateTime lastPhq9At;

    /** Latest mood log snapshot */
    @Column(length = 32)
    private String lastMood;

    private Integer lastMoodScore; // 0-10

    private LocalDateTime lastMoodAt;

    /** User-editable preferences */
    @Column(length = 32)
    private String preferredTone; // e.g. 温和/鼓励/直接

    @Column(length = 2000)
    private String goals; // what the user wants to improve

    @Column(length = 2000)
    private String triggers; // common triggers/stressors

    @Column(length = 2000)
    private String copingPreferences; // what helps / what to avoid

    @Column(length = 32)
    private String riskLevel; // LOW/MEDIUM/HIGH (derived)

    /** Conversation-derived (LLM-extracted) insights. Kept conservative & editable. */
    @Column(length = 512)
    private String communicationStyle;

    @Column(length = 1000)
    private String personalityNotes;

    @Column(length = 2000)
    private String insightEvidence;

    private Double insightConfidence; // 0~1

    private LocalDateTime lastInsightAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public UserProfile() {
    }

    public UserProfile(User user) {
        this.user = user;
        this.updatedAt = LocalDateTime.now();
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

    public Integer getLastPhq9Score() {
        return lastPhq9Score;
    }

    public void setLastPhq9Score(Integer lastPhq9Score) {
        this.lastPhq9Score = lastPhq9Score;
    }

    public String getLastPhq9Severity() {
        return lastPhq9Severity;
    }

    public void setLastPhq9Severity(String lastPhq9Severity) {
        this.lastPhq9Severity = lastPhq9Severity;
    }

    public LocalDateTime getLastPhq9At() {
        return lastPhq9At;
    }

    public void setLastPhq9At(LocalDateTime lastPhq9At) {
        this.lastPhq9At = lastPhq9At;
    }

    public String getLastMood() {
        return lastMood;
    }

    public void setLastMood(String lastMood) {
        this.lastMood = lastMood;
    }

    public Integer getLastMoodScore() {
        return lastMoodScore;
    }

    public void setLastMoodScore(Integer lastMoodScore) {
        this.lastMoodScore = lastMoodScore;
    }

    public LocalDateTime getLastMoodAt() {
        return lastMoodAt;
    }

    public void setLastMoodAt(LocalDateTime lastMoodAt) {
        this.lastMoodAt = lastMoodAt;
    }

    public String getPreferredTone() {
        return preferredTone;
    }

    public void setPreferredTone(String preferredTone) {
        this.preferredTone = preferredTone;
    }

    public String getGoals() {
        return goals;
    }

    public void setGoals(String goals) {
        this.goals = goals;
    }

    public String getTriggers() {
        return triggers;
    }

    public void setTriggers(String triggers) {
        this.triggers = triggers;
    }

    public String getCopingPreferences() {
        return copingPreferences;
    }

    public void setCopingPreferences(String copingPreferences) {
        this.copingPreferences = copingPreferences;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getCommunicationStyle() {
        return communicationStyle;
    }

    public void setCommunicationStyle(String communicationStyle) {
        this.communicationStyle = communicationStyle;
    }

    public String getPersonalityNotes() {
        return personalityNotes;
    }

    public void setPersonalityNotes(String personalityNotes) {
        this.personalityNotes = personalityNotes;
    }

    public String getInsightEvidence() {
        return insightEvidence;
    }

    public void setInsightEvidence(String insightEvidence) {
        this.insightEvidence = insightEvidence;
    }

    public Double getInsightConfidence() {
        return insightConfidence;
    }

    public void setInsightConfidence(Double insightConfidence) {
        this.insightConfidence = insightConfidence;
    }

    public LocalDateTime getLastInsightAt() {
        return lastInsightAt;
    }

    public void setLastInsightAt(LocalDateTime lastInsightAt) {
        this.lastInsightAt = lastInsightAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }
}
