package com.example.psyaihealer.profile;

import com.example.psyaihealer.user.User;

import java.time.LocalDateTime;

/**
 * Flattened view for admin/counselor UI visualization.
 * Avoids exposing internal entity graphs and keeps payload stable.
 */
public class UserProfileViewDto {

    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private String role;
    private boolean enabled;

    private Long profileId;
    private Integer lastPhq9Score;
    private String lastPhq9Severity;
    private LocalDateTime lastPhq9At;

    private String lastMood;
    private Integer lastMoodScore;
    private LocalDateTime lastMoodAt;

    private String preferredTone;
    private String goals;
    private String triggers;
    private String copingPreferences;

    private String riskLevel;
    private LocalDateTime updatedAt;

    private String communicationStyle;
    private String personalityNotes;
    private String insightEvidence;
    private Double insightConfidence;
    private LocalDateTime lastInsightAt;

    public static UserProfileViewDto of(User user, UserProfile profile) {
        UserProfileViewDto dto = new UserProfileViewDto();
        dto.userId = user.getId();
        dto.username = user.getUsername();
        dto.fullName = user.getFullName();
        dto.email = user.getEmail();
        dto.role = user.getRole() != null ? user.getRole().name() : null;
        dto.enabled = user.isEnabled();

        dto.profileId = profile.getId();
        dto.lastPhq9Score = profile.getLastPhq9Score();
        dto.lastPhq9Severity = profile.getLastPhq9Severity();
        dto.lastPhq9At = profile.getLastPhq9At();

        dto.lastMood = profile.getLastMood();
        dto.lastMoodScore = profile.getLastMoodScore();
        dto.lastMoodAt = profile.getLastMoodAt();

        dto.preferredTone = profile.getPreferredTone();
        dto.goals = profile.getGoals();
        dto.triggers = profile.getTriggers();
        dto.copingPreferences = profile.getCopingPreferences();

        dto.riskLevel = profile.getRiskLevel();
        dto.updatedAt = profile.getUpdatedAt();

        dto.communicationStyle = profile.getCommunicationStyle();
        dto.personalityNotes = profile.getPersonalityNotes();
        dto.insightEvidence = profile.getInsightEvidence();
        dto.insightConfidence = profile.getInsightConfidence();
        dto.lastInsightAt = profile.getLastInsightAt();
        return dto;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Long getProfileId() {
        return profileId;
    }

    public Integer getLastPhq9Score() {
        return lastPhq9Score;
    }

    public String getLastPhq9Severity() {
        return lastPhq9Severity;
    }

    public LocalDateTime getLastPhq9At() {
        return lastPhq9At;
    }

    public String getLastMood() {
        return lastMood;
    }

    public Integer getLastMoodScore() {
        return lastMoodScore;
    }

    public LocalDateTime getLastMoodAt() {
        return lastMoodAt;
    }

    public String getPreferredTone() {
        return preferredTone;
    }

    public String getGoals() {
        return goals;
    }

    public String getTriggers() {
        return triggers;
    }

    public String getCopingPreferences() {
        return copingPreferences;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getCommunicationStyle() {
        return communicationStyle;
    }

    public String getPersonalityNotes() {
        return personalityNotes;
    }

    public String getInsightEvidence() {
        return insightEvidence;
    }

    public Double getInsightConfidence() {
        return insightConfidence;
    }

    public LocalDateTime getLastInsightAt() {
        return lastInsightAt;
    }
}
