package com.example.psyaihealer.growth;

import com.example.psyaihealer.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GrowthLogRepository extends JpaRepository<GrowthLog, Long> {
    List<GrowthLog> findByUserOrderByCreatedAtAsc(User user);
}
