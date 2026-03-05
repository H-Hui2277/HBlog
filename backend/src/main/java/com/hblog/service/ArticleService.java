package com.hblog.service;

import com.hblog.dto.ArticleDTO;
import com.hblog.entity.Article;
import com.hblog.entity.Category;
import com.hblog.repository.ArticleRepository;
import com.hblog.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CategoryRepository categoryRepository;

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

        // Handle categories
        if (articleDTO.getCategoryIds() != null && !articleDTO.getCategoryIds().isEmpty()) {
            Set<Category> categories = new HashSet<>(categoryRepository.findAllById(articleDTO.getCategoryIds()));
            article.setCategories(categories);
        }

        Article saved = articleRepository.save(article);
        return toDTO(saved);
    }

    public Optional<ArticleDTO> updateArticle(Long id, ArticleDTO articleDTO) {
        return articleRepository.findById(id).map(article -> {
            article.setTitle(articleDTO.getTitle());
            article.setContent(articleDTO.getContent());
            article.setImageUrl(articleDTO.getImageUrl());

            // Handle categories
            if (articleDTO.getCategoryIds() != null) {
                Set<Category> categories = new HashSet<>(categoryRepository.findAllById(articleDTO.getCategoryIds()));
                article.setCategories(categories);
            } else {
                article.setCategories(new HashSet<>());
            }

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
        ArticleDTO dto = new ArticleDTO(
                article.getId(),
                article.getTitle(),
                article.getContent(),
                article.getImageUrl(),
                article.getCreatedAt(),
                article.getUpdatedAt()
        );

        // Convert categories to categoryIds
        Set<Long> categoryIds = article.getCategories().stream()
                .map(Category::getId)
                .collect(Collectors.toSet());
        dto.setCategoryIds(categoryIds);

        return dto;
    }
}
