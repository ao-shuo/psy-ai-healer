package com.example.psyaihealer.knowledge;

import com.example.psyaihealer.dto.ArticleRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeService {

    private final KnowledgeArticleRepository repository;

    public KnowledgeService(KnowledgeArticleRepository repository) {
        this.repository = repository;
    }

    public KnowledgeArticle create(ArticleRequest request) {
        KnowledgeArticle article = new KnowledgeArticle(
                request.getTitle(),
                request.getCategory(),
                request.getContent(),
                request.isPublished()
        );
        return repository.save(article);
    }

    public KnowledgeArticle update(Long id, ArticleRequest request) {
        KnowledgeArticle article = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("文章不存在"));
        article.setTitle(request.getTitle());
        article.setCategory(request.getCategory());
        article.setContent(request.getContent());
        article.setPublished(request.isPublished());
        article.touchUpdatedAt();
        return repository.save(article);
    }

    public List<KnowledgeArticle> listPublished() {
        return repository.findByPublishedTrue();
    }

    public List<KnowledgeArticle> listAll() {
        return repository.findAll();
    }

    public List<KnowledgeArticle> byCategory(String category) {
        return repository.findByCategoryIgnoreCase(category);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
