package com.example.psyaihealer.profile;

import com.example.psyaihealer.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserProfileService {

    private final UserProfileRepository repository;

    public UserProfileService(UserProfileRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public UserProfile getOrCreate(User user) {
        return repository.findByUser(user).orElseGet(() -> repository.save(new UserProfile(user)));
    }

    @Transactional
    public UserProfile updateFromPhq9(User user, int score, String severity) {
        UserProfile profile = getOrCreate(user);
        profile.setLastPhq9Score(score);
        profile.setLastPhq9Severity(severity);
        profile.setLastPhq9At(LocalDateTime.now());
        profile.setRiskLevel(deriveRiskLevel(score, profile.getLastMoodScore()));
        profile.touch();
        return repository.save(profile);
    }

    @Transactional
    public UserProfile updateFromMood(User user, String mood, Integer moodScore) {
        UserProfile profile = getOrCreate(user);
        if (mood != null && !mood.isBlank()) {
            profile.setLastMood(mood.trim());
        }
        if (moodScore != null) {
            profile.setLastMoodScore(moodScore);
        }
        profile.setLastMoodAt(LocalDateTime.now());
        Integer phq9 = profile.getLastPhq9Score();
        profile.setRiskLevel(deriveRiskLevel(phq9 == null ? 0 : phq9, profile.getLastMoodScore()));
        profile.touch();
        return repository.save(profile);
    }

    @Transactional
    public UserProfile patchPreferences(User user, String preferredTone, String goals, String triggers, String copingPreferences) {
        UserProfile profile = getOrCreate(user);
        if (preferredTone != null) {
            profile.setPreferredTone(trimToNull(preferredTone, 32));
        }
        if (goals != null) {
            profile.setGoals(trimToNull(goals, 2000));
        }
        if (triggers != null) {
            profile.setTriggers(trimToNull(triggers, 2000));
        }
        if (copingPreferences != null) {
            profile.setCopingPreferences(trimToNull(copingPreferences, 2000));
        }
        profile.touch();
        return repository.save(profile);
    }

    @Transactional
    public UserProfile applyConversationInsights(User user, UserProfileInsights insights) {
        if (insights == null) {
            return getOrCreate(user);
        }
        UserProfile profile = getOrCreate(user);

        boolean changed = false;

        String tone = trimToNull(insights.preferredTone(), 32);
        if (tone != null && !tone.isBlank()) {
            if (profile.getPreferredTone() == null || profile.getPreferredTone().isBlank()) {
                profile.setPreferredTone(tone);
                changed = true;
            }
        }

        String goals = trimToNull(insights.goals(), 2000);
        if (goals != null && !goals.isBlank()) {
            String merged = mergeText(profile.getGoals(), goals, 2000);
            if (!merged.equals(profile.getGoals() == null ? "" : profile.getGoals())) {
                profile.setGoals(merged);
                changed = true;
            }
        }

        String triggers = trimToNull(insights.triggers(), 2000);
        if (triggers != null && !triggers.isBlank()) {
            String merged = mergeText(profile.getTriggers(), triggers, 2000);
            if (!merged.equals(profile.getTriggers() == null ? "" : profile.getTriggers())) {
                profile.setTriggers(merged);
                changed = true;
            }
        }

        String coping = trimToNull(insights.copingPreferences(), 2000);
        if (coping != null && !coping.isBlank()) {
            String merged = mergeText(profile.getCopingPreferences(), coping, 2000);
            if (!merged.equals(profile.getCopingPreferences() == null ? "" : profile.getCopingPreferences())) {
                profile.setCopingPreferences(merged);
                changed = true;
            }
        }

        String style = trimToNull(insights.communicationStyle(), 512);
        if (style != null && !style.isBlank()) {
            // Only set if empty to avoid overfitting to one conversation.
            if (profile.getCommunicationStyle() == null || profile.getCommunicationStyle().isBlank()) {
                profile.setCommunicationStyle(style);
                changed = true;
            }
        }

        String notes = trimToNull(insights.personalityNotes(), 1000);
        if (notes != null && !notes.isBlank()) {
            // Merge lightly; keep short.
            String merged = mergeText(profile.getPersonalityNotes(), notes, 1000);
            if (!merged.equals(profile.getPersonalityNotes() == null ? "" : profile.getPersonalityNotes())) {
                profile.setPersonalityNotes(merged);
                changed = true;
            }
        }

        String evidence = trimToNull(insights.evidence(), 2000);
        if (evidence != null && !evidence.isBlank()) {
            String merged = mergeText(profile.getInsightEvidence(), evidence, 2000);
            if (!merged.equals(profile.getInsightEvidence() == null ? "" : profile.getInsightEvidence())) {
                profile.setInsightEvidence(merged);
                changed = true;
            }
        }

        Double confidence = insights.confidence();
        if (confidence != null && confidence >= 0 && confidence <= 1) {
            // Keep the max confidence seen so far for visibility.
            Double prev = profile.getInsightConfidence();
            if (prev == null || confidence > prev) {
                profile.setInsightConfidence(confidence);
                changed = true;
            }
        }

        if (changed) {
            profile.setLastInsightAt(LocalDateTime.now());
            profile.touch();
            return repository.save(profile);
        }

        return profile;
    }

    public String buildPromptContext(UserProfile profile) {
        if (profile == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("用户画像摘要（用于更贴合的支持性对话；不用于下诊断）：\n");

        if (profile.getPreferredTone() != null && !profile.getPreferredTone().isBlank()) {
            sb.append("- 期望语气：").append(profile.getPreferredTone()).append("\n");
        }
        if (profile.getGoals() != null && !profile.getGoals().isBlank()) {
            sb.append("- 目标：").append(profile.getGoals()).append("\n");
        }
        if (profile.getTriggers() != null && !profile.getTriggers().isBlank()) {
            sb.append("- 常见触发点/压力源：").append(profile.getTriggers()).append("\n");
        }
        if (profile.getCopingPreferences() != null && !profile.getCopingPreferences().isBlank()) {
            sb.append("- 偏好/有效方式：").append(profile.getCopingPreferences()).append("\n");
        }
        if (profile.getLastPhq9Score() != null) {
            sb.append("- 最近 PHQ-9：").append(profile.getLastPhq9Score())
                    .append(" 分（").append(nullToEmpty(profile.getLastPhq9Severity())).append("）\n");
        }
        if (profile.getLastMoodScore() != null || (profile.getLastMood() != null && !profile.getLastMood().isBlank())) {
            sb.append("- 最近心情：");
            if (profile.getLastMood() != null && !profile.getLastMood().isBlank()) {
                sb.append(profile.getLastMood());
            }
            if (profile.getLastMoodScore() != null) {
                sb.append("（").append(profile.getLastMoodScore()).append("/10）");
            }
            sb.append("\n");
        }
        if (profile.getRiskLevel() != null && !profile.getRiskLevel().isBlank()) {
            sb.append("- 风险级别：").append(profile.getRiskLevel()).append("\n");
        }

        return sb.toString().trim();
    }

    private static String deriveRiskLevel(int phq9Score, Integer moodScore) {
        // Simple heuristic: PHQ-9 >= 15 => HIGH; 10-14 => MEDIUM; else LOW.
        if (phq9Score >= 15) {
            return "HIGH";
        }
        if (phq9Score >= 10) {
            return "MEDIUM";
        }
        // Mood score very low can nudge to medium.
        if (moodScore != null && moodScore <= 2) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private static String trimToNull(String v, int maxLen) {
        if (v == null) return null;
        String t = v.trim();
        if (t.isBlank()) return "";
        if (t.length() > maxLen) {
            return t.substring(0, maxLen);
        }
        return t;
    }

    private static String mergeText(String existing, String addition, int maxLen) {
        String base = existing == null ? "" : existing.trim();
        String add = addition == null ? "" : addition.trim();
        if (add.isBlank()) {
            return base;
        }
        if (base.isBlank()) {
            return add.length() > maxLen ? add.substring(0, maxLen) : add;
        }

        // Avoid obvious duplication.
        String normalizedBase = base.replaceAll("\\s+", " ");
        String normalizedAdd = add.replaceAll("\\s+", " ");
        if (normalizedBase.contains(normalizedAdd)) {
            return base;
        }

        String merged = base + "\n" + add;
        if (merged.length() > maxLen) {
            merged = merged.substring(0, maxLen);
        }
        return merged;
    }

    private static String nullToEmpty(String v) {
        return v == null ? "" : v;
    }
}
