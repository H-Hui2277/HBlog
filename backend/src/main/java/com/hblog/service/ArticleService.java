package com.hblog.service;

import com.hblog.dto.ArticleDTO;
import com.hblog.entity.Article;
import com.hblog.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    public List<ArticleDTO> getAllArticles() {
        return articleRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<ArticleDTO> getArticleById(Long id) {
        return articleRepository.findById(id).map(this::toDTO);
    }

    public ArticleDTO createArticle(ArticleDTO articleDTO) {
        Article article = new Article();
        article.setTitle(articleDTO.getTitle());
        article.setContent(articleDTO.getContent());
        article.setImageUrl(articleDTO.getImageUrl());

        Article saved = articleRepository.save(article);
        return toDTO(saved);
    }

    public Optional<ArticleDTO> updateArticle(Long id, ArticleDTO articleDTO) {
        return articleRepository.findById(id).map(article -> {
            article.setTitle(articleDTO.getTitle());
            article.setContent(articleDTO.getContent());
            article.setImageUrl(articleDTO.getImageUrl());
            return toDTO(articleRepository.save(article));
        });
    }

    public boolean deleteArticle(Long id) {
        if (articleRepository.existsById(id)) {
            articleRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private ArticleDTO toDTO(Article article) {
        return new ArticleDTO(
                article.getId(),
                article.getTitle(),
                article.getContent(),
                article.getImageUrl(),
                article.getCreatedAt(),
                article.getUpdatedAt()
        );
    }
}
