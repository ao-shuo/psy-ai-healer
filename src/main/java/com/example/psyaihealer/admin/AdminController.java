package com.example.psyaihealer.admin;

import com.example.psyaihealer.knowledge.KnowledgeArticle;
import com.example.psyaihealer.knowledge.KnowledgeService;
import com.example.psyaihealer.user.User;
import com.example.psyaihealer.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public ResponseEntity<List<User>> users() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/articles")
    public ResponseEntity<List<KnowledgeArticle>> articles() {
        return ResponseEntity.ok(knowledgeService.listAll());
    }
}
