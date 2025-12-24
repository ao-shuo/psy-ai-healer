package com.example.psyaihealer.knowledge;

import com.example.psyaihealer.dto.ArticleRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    public KnowledgeController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @GetMapping
    public ResponseEntity<List<KnowledgeArticle>> published() {
        return ResponseEntity.ok(knowledgeService.listPublished());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<KnowledgeArticle>> byCategory(@PathVariable String category) {
        return ResponseEntity.ok(knowledgeService.byCategory(category));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<KnowledgeArticle> create(@RequestBody ArticleRequest request) {
        return ResponseEntity.ok(knowledgeService.create(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<KnowledgeArticle> update(@PathVariable Long id, @RequestBody ArticleRequest request) {
        return ResponseEntity.ok(knowledgeService.update(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        knowledgeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<KnowledgeArticle>> all() {
        return ResponseEntity.ok(knowledgeService.listAll());
    }
}
