package com.example.psyaihealer.growth;

import com.example.psyaihealer.user.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GrowthLogService {

    private final GrowthLogRepository repository;

    public GrowthLogService(GrowthLogRepository repository) {
        this.repository = repository;
    }

    public GrowthLog create(User user, String mood, Integer moodScore, String content) {
        if (moodScore != null && (moodScore < 0 || moodScore > 10)) {
            throw new IllegalArgumentException("心情评分需在0-10之间");
        }
        GrowthLog log = new GrowthLog(user, mood, moodScore, content);
        return repository.save(log);
    }

    public List<GrowthLog> list(User user) {
        return repository.findByUserOrderByCreatedAtAsc(user);
    }
}
