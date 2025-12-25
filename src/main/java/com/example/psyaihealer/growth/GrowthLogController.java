package com.example.psyaihealer.growth;

import com.example.psyaihealer.user.User;
import com.example.psyaihealer.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/growth")
public class GrowthLogController {

    private final GrowthLogService service;
    private final UserRepository userRepository;

    public GrowthLogController(GrowthLogService service, UserRepository userRepository) {
        this.service = service;
        this.userRepository = userRepository;
    }

    @PostMapping("/logs")
    public ResponseEntity<GrowthLog> create(@AuthenticationPrincipal UserDetails principal,
                                            @RequestBody Map<String, Object> body) {
        User user = userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        String mood = (String) body.getOrDefault("mood", "");
        Integer moodScore = body.get("moodScore") instanceof Number ? ((Number) body.get("moodScore")).intValue() : null;
        String content = (String) body.getOrDefault("content", "");
        GrowthLog log = service.create(user, mood, moodScore, content);
        return ResponseEntity.ok(log);
    }

    @GetMapping("/logs")
    public ResponseEntity<List<GrowthLog>> list(@AuthenticationPrincipal UserDetails principal) {
        User user = userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        return ResponseEntity.ok(service.list(user));
    }
}
