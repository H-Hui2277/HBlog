package com.hblog.service;

import com.hblog.dto.CategoryDTO;
import com.hblog.entity.Category;
import com.hblog.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<CategoryDTO> getAllCategoriesTree() {
        List<Category> rootCategories = categoryRepository.findByParentIdIsNullOrderBySortOrderAsc();
        return rootCategories.stream()
                .map(this::buildTree)
                .collect(Collectors.toList());
    }

    public Optional<CategoryDTO> getCategoryById(Long id) {
        return categoryRepository.findById(id).map(this::toDTO);
    }

    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        // Generate slug if not provided
        if (categoryDTO.getSlug() == null || categoryDTO.getSlug().isEmpty()) {
            categoryDTO.setSlug(generateSlug(categoryDTO.getName()));
        }

        // Check for duplicate slug
        if (categoryRepository.existsBySlug(categoryDTO.getSlug())) {
            throw new RuntimeException("分类slug已存在: " + categoryDTO.getSlug());
        }

        // Set default sort order
        if (categoryDTO.getSortOrder() == null) {
            categoryDTO.setSortOrder(0);
        }

        Category category = new Category();
        category.setName(categoryDTO.getName());
        category.setSlug(categoryDTO.getSlug());
        category.setParentId(categoryDTO.getParentId());
        category.setSortOrder(categoryDTO.getSortOrder());

        Category saved = categoryRepository.save(category);
        return toDTO(saved);
    }

    @Transactional
    public Optional<CategoryDTO> updateCategory(Long id, CategoryDTO categoryDTO) {
        return categoryRepository.findById(id).map(category -> {
            // Handle name and slug
            if (categoryDTO.getName() != null) {
                category.setName(categoryDTO.getName());
                // Auto-generate slug if not explicitly set
                if (categoryDTO.getSlug() == null || categoryDTO.getSlug().equals(generateSlug(category.getName()))) {
                    category.setSlug(generateSlug(categoryDTO.getName()));
                }
            }

            // Handle slug explicitly set (check for duplicates)
            if (categoryDTO.getSlug() != null && !categoryDTO.getSlug().equals(category.getSlug())) {
                if (categoryRepository.existsBySlugAndIdNot(categoryDTO.getSlug(), id)) {
                    throw new RuntimeException("分类slug已存在: " + categoryDTO.getSlug());
                }
                category.setSlug(categoryDTO.getSlug());
            }

            // Handle parentId (including setting to null for root category)
            if (categoryDTO.getParentId() != null) {
                // Prevent setting itself as parent
                if (categoryDTO.getParentId().equals(id)) {
                    throw new RuntimeException("不能将分类设置为其自身的父分类");
                }
            }
            category.setParentId(categoryDTO.getParentId());

            if (categoryDTO.getSortOrder() != null) {
                category.setSortOrder(categoryDTO.getSortOrder());
            }

            return toDTO(categoryRepository.save(category));
        });
    }

    @Transactional
    public boolean deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            return false;
        }

        // Check if there are children
        List<Category> children = categoryRepository.findByParentIdOrderBySortOrderAsc(id);
        if (!children.isEmpty()) {
            throw new RuntimeException("该分类下有子分类，无法删除");
        }

        categoryRepository.deleteById(id);
        return true;
    }

    private CategoryDTO buildTree(Category category) {
        CategoryDTO dto = toDTO(category);
        List<Category> children = categoryRepository.findByParentIdOrderBySortOrderAsc(category.getId());
        dto.setChildren(children.stream()
                .map(this::buildTree)
                .collect(Collectors.toList()));
        return dto;
    }

    private CategoryDTO toDTO(Category category) {
        return new CategoryDTO(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getParentId(),
                category.getSortOrder(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    private String generateSlug(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        return name.toLowerCase()
                .replaceAll("[\\s\\u4e00-\\u9fa5]+", "-")
                .replaceAll("[^a-z0-9-]", "")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}
