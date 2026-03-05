package com.hblog.controller;

import com.hblog.dto.CategoryDTO;
import com.hblog.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    @Autowired
    private CategoryService categoryService;

    // Get all categories (flat list)
    @GetMapping
    public List<CategoryDTO> getAllCategories() {
        logger.info("Getting all categories");
        return categoryService.getAllCategories();
    }

    // Get category tree
    @GetMapping("/tree")
    public List<CategoryDTO> getCategoryTree() {
        logger.info("Getting category tree");
        return categoryService.getAllCategoriesTree();
    }

    // Get category by ID
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        logger.info("Getting category by id: {}", id);
        return categoryService.getCategoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Create category
    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryDTO categoryDTO) {
        logger.info("Creating category: name={}, parentId={}", categoryDTO.getName(), categoryDTO.getParentId());
        try {
            CategoryDTO created = categoryService.createCategory(categoryDTO);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            logger.error("Error creating category", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Update category
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryDTO categoryDTO) {
        logger.info("Updating category: id={}, name={}", id, categoryDTO.getName());
        try {
            return categoryService.updateCategory(id, categoryDTO)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error updating category", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Delete category
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        logger.info("Deleting category: id={}", id);
        try {
            if (categoryService.deleteCategory(id)) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting category", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
