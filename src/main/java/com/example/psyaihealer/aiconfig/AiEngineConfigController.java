package com.example.psyaihealer.aiconfig;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai-config")
public class AiEngineConfigController {

    private final AiEngineConfigRepository repository;

    public AiEngineConfigController(AiEngineConfigRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AiEngineConfig>> list() {
        return ResponseEntity.ok(repository.findAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AiEngineConfig> create(@RequestBody AiEngineConfig config) {
        return ResponseEntity.ok(repository.save(config));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AiEngineConfig> update(@PathVariable Long id, @RequestBody AiEngineConfig body) {
        AiEngineConfig existing = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("配置不存在"));
        existing.setAvatarName(body.getAvatarName());
        existing.setVoice(body.getVoice());
        existing.setDialogRules(body.getDialogRules());
        return ResponseEntity.ok(repository.save(existing));
    }
}
