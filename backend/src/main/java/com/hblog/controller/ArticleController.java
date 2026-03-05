package com.hblog.controller;

import com.hblog.dto.ArticleDTO;
import com.hblog.service.ArticleService;
import com.hblog.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/articles")
@CrossOrigin(origins = "*")
public class ArticleController {

    private static final Logger logger = LoggerFactory.getLogger(ArticleController.class);

    @Autowired
    private ArticleService articleService;

    @Autowired
    private FileService fileService;

    // Public endpoints
    @GetMapping
    public List<ArticleDTO> getAllArticles() {
        return articleService.getAllArticles();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticleDTO> getArticleById(@PathVariable Long id) {
        return articleService.getArticleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Admin endpoints
    @PostMapping
    public ResponseEntity<ArticleDTO> createArticle(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam(required = false) String categoryIds) {

        logger.info("Creating article: title={}, content length={}, has image={}", title, content != null ? content.length() : 0, image != null && !image.isEmpty());

        try {
            ArticleDTO dto = new ArticleDTO();
            dto.setTitle(title);
            dto.setContent(content != null ? content : "");

            // Handle categoryIds
            if (categoryIds != null && !categoryIds.isEmpty()) {
                Set<Long> ids = Arrays.stream(categoryIds.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::parseLong)
                        .collect(Collectors.toSet());
                dto.setCategoryIds(ids);
            }

            if (image != null && !image.isEmpty()) {
                String imageUrl = fileService.uploadFile(image);
                dto.setImageUrl(imageUrl);
                logger.info("Image uploaded: {}", imageUrl);
            }

            ArticleDTO created = articleService.createArticle(dto);
            return ResponseEntity.ok(created);
        } catch (IOException e) {
            logger.error("Error creating article", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArticleDTO> updateArticle(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam(required = false) String removeImage,
            @RequestParam(required = false) String categoryIds) {

        logger.info("Updating article: id={}, title={}, content length={}", id, title, content != null ? content.length() : 0);

        try {
            ArticleDTO dto = new ArticleDTO();
            dto.setTitle(title);
            dto.setContent(content != null ? content : "");

            // Handle image update
            if ("true".equals(removeImage)) {
                dto.setImageUrl(null);
                logger.info("Removing image for article {}", id);
            } else if (image != null && !image.isEmpty()) {
                String imageUrl = fileService.uploadFile(image);
                dto.setImageUrl(imageUrl);
                logger.info("Image uploaded: {}", imageUrl);
            } else {
                // Keep existing image
                dto.setImageUrl(articleService.getArticleById(id)
                        .map(ArticleDTO::getImageUrl)
                        .orElse(null));
            }

            // Handle categoryIds
            if (categoryIds != null && !categoryIds.isEmpty()) {
                Set<Long> ids = Arrays.stream(categoryIds.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::parseLong)
                        .collect(Collectors.toSet());
                dto.setCategoryIds(ids);
            } else {
                dto.setCategoryIds(new HashSet<>());
            }

            return articleService.updateArticle(id, dto)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IOException e) {
            logger.error("Error updating article", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        if (articleService.deleteArticle(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
