package com.example.bookstore.service;

import com.example.bookstore.domain.Category;
import com.example.bookstore.dto.CategoryPatchRequest;
import com.example.bookstore.dto.CategoryRequest;
import com.example.bookstore.dto.CategoryResponse;
import com.example.bookstore.exception.CategoryNotFoundException;
import com.example.bookstore.exception.DuplicateResourceException;
import com.example.bookstore.mapper.CategoryMapper;
import com.example.bookstore.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse findById(UUID id) {
        return categoryRepository.findById(id)
                .map(categoryMapper::toResponse)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + id));
    }

    @Transactional
    public CategoryResponse save(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Category name already exists: " + request.getName());
        }
        Category category = categoryMapper.toEntity(request);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(CategoryRequest request, UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException("Category not found: " + id);
        }

        categoryRepository.findByNameIgnoreCase(request.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new DuplicateResourceException("Category name already exists: " + request.getName());
            }
        });

        Category category = categoryMapper.toEntity(request);
        category.setId(id);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse patch(UUID id, CategoryPatchRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + id));

        if (request.getName() != null) {
            categoryRepository.findByNameIgnoreCase(request.getName()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new DuplicateResourceException("Category name already exists: " + request.getName());
                }
            });
        }

        categoryMapper.patchEntityFromRequest(request, category);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void delete(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException("Category not found: " + id);
        }
        categoryRepository.deleteById(id);
    }
}