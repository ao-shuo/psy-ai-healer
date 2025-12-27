package com.example.psyaihealer.admin;

import com.example.psyaihealer.knowledge.KnowledgeArticle;
import com.example.psyaihealer.knowledge.KnowledgeService;
import com.example.psyaihealer.user.Role;
import com.example.psyaihealer.user.User;
import com.example.psyaihealer.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final KnowledgeService knowledgeService;

    public AdminController(UserRepository userRepository, KnowledgeService knowledgeService) {
        this.userRepository = userRepository;
        this.knowledgeService = knowledgeService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> users(@RequestParam(name = "q", required = false) String q) {
        if (q == null || q.isBlank()) {
            return ResponseEntity.ok(userRepository.findAll());
        }
        return ResponseEntity.ok(userRepository.search(q.trim()));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> user(@PathVariable Long id) {
        return ResponseEntity.ok(userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在")));
    }

    @GetMapping("/users/pending")
    public ResponseEntity<List<User>> pendingUsers() {
        return ResponseEntity.ok(userRepository.findByEnabledFalse());
    }

    @PatchMapping("/users/{id}/enable")
    public ResponseEntity<Map<String, Object>> enableUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        user.setEnabled(true);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of(
                "message", "已启用用户",
                "id", user.getId(),
                "username", user.getUsername(),
                "role", user.getRole().name(),
                "enabled", user.isEnabled()
        ));
    }

    @PatchMapping("/users/{id}/disable")
    public ResponseEntity<Map<String, Object>> disableUser(@PathVariable Long id,
                                                          @AuthenticationPrincipal UserDetails principal) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        if (principal != null && principal.getUsername() != null
                && principal.getUsername().equals(user.getUsername())) {
            throw new IllegalArgumentException("不能禁用自己");
        }

        if (user.getRole() == Role.ADMIN && user.isEnabled()) {
            long enabledAdmins = userRepository.countByRoleAndEnabledTrue(Role.ADMIN);
            if (enabledAdmins <= 1) {
                throw new IllegalArgumentException("不能禁用最后一个管理员账号");
            }
        }

        user.setEnabled(false);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of(
                "message", "已禁用用户",
                "id", user.getId(),
                "username", user.getUsername(),
                "role", user.getRole().name(),
                "enabled", user.isEnabled()
        ));
    }

    @GetMapping("/articles")
    public ResponseEntity<List<KnowledgeArticle>> articles() {
        return ResponseEntity.ok(knowledgeService.listAll());
    }
}
