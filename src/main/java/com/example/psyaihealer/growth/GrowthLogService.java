package com.example.psyaihealer.growth;

import com.example.psyaihealer.profile.UserProfileService;
import com.example.psyaihealer.user.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GrowthLogService {

    private final GrowthLogRepository repository;
    private final UserProfileService profileService;

    public GrowthLogService(GrowthLogRepository repository, UserProfileService profileService) {
        this.repository = repository;
        this.profileService = profileService;
    }

    public GrowthLog create(User user, String mood, Integer moodScore, String content) {
        if (moodScore != null && (moodScore < 0 || moodScore > 10)) {
            throw new IllegalArgumentException("心情评分需在0-10之间");
        }
        GrowthLog log = new GrowthLog(user, mood, moodScore, content);
        GrowthLog saved = repository.save(log);

        // Keep the user profile up-to-date for personalized therapy replies.
        profileService.updateFromMood(user, mood, moodScore);

        return saved;
    }

    public List<GrowthLog> list(User user) {
        return repository.findByUserOrderByCreatedAtAsc(user);
    }
}
