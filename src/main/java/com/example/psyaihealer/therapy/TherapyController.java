package com.example.psyaihealer.therapy;

import com.example.psyaihealer.dto.ChatMessage;
import com.example.psyaihealer.dto.ChatResponse;
import com.example.psyaihealer.user.User;
import com.example.psyaihealer.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/therapy")
public class TherapyController {

    private final TherapyService therapyService;
    private final UserRepository userRepository;

    public TherapyController(TherapyService therapyService, UserRepository userRepository) {
        this.therapyService = therapyService;
        this.userRepository = userRepository;
    }

    @PostMapping("/sessions")
    public ResponseEntity<TherapySession> create(@AuthenticationPrincipal UserDetails principal,
                                                 @RequestBody(required = false) Map<String, String> body) {
        User user = userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        String topic = body != null ? body.getOrDefault("topic", "情绪疏导") : "情绪疏导";
        TherapySession session = therapyService.createSession(user, topic);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<TherapySession>> list(@AuthenticationPrincipal UserDetails principal) {
        User user = userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        return ResponseEntity.ok(therapyService.sessionsFor(user));
    }

    @GetMapping("/sessions/{id}/messages")
    public ResponseEntity<List<TherapyMessage>> messages(@PathVariable Long id) {
        return ResponseEntity.ok(therapyService.messages(id));
    }

    @PostMapping("/sessions/{id}/message")
    public ResponseEntity<ChatResponse> send(@PathVariable Long id, @RequestBody ChatMessage message) {
        TherapySession session = therapyService.getSessionOrThrow(id);
        ChatResponse response = therapyService.processMessage(session, message.getContent());
        return ResponseEntity.ok(response);
    }
}
