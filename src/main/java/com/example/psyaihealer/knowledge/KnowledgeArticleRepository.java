package com.example.psyaihealer.knowledge;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {
    List<KnowledgeArticle> findByPublishedTrue();
    List<KnowledgeArticle> findByCategoryIgnoreCase(String category);
}
