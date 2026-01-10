package com.example.psyaihealer.profile;

/**
 * Incremental profile insights extracted from conversation.
 * Fields are optional; empty/blank means "unknown / do not update".
 */
public record UserProfileInsights(
        String preferredTone,
        String goals,
        String triggers,
        String copingPreferences,
        String communicationStyle,
        String personalityNotes,
        String evidence,
        Double confidence
) {
}
