package com.example.bookstore.service;

import com.example.bookstore.domain.Category;
import com.example.bookstore.dto.CategoryPatchRequest;
import com.example.bookstore.dto.CategoryRequest;
import com.example.bookstore.dto.CategoryResponse;
import com.example.bookstore.exception.CategoryNotFoundException;
import com.example.bookstore.exception.DuplicateResourceException;
import com.example.bookstore.mapper.CategoryMapper;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;
    private final CategoryMapper categoryMapper;

    private Pageable createPageable(Integer page, Integer size, String sort) {
        Sort sortObj = Sort.unsorted();
        if (sort != null && sort.contains(",")) {
            String[] sortParams = sort.split(",");
            sortObj = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
        } else if (sort != null) {
            sortObj = Sort.by(Sort.Direction.ASC, sort);
        }
        return PageRequest.of(page != null ? page : 0, size != null ? size : 20, sortObj);
    }

    @Transactional(readOnly = true)
    public Page<CategoryResponse> searchCategories(String keyword, Boolean active, Integer page, Integer size, String sort) {
        Pageable pageable = createPageable(page, size, sort);

        return categoryRepository.searchCategories(keyword, active, pageable)
                .map(categoryMapper::toResponse);
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

        if (category.getActive() == null) {
            category.setActive(true);
        }

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
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + id));

        if (bookRepository.existsByCategory_Id(id)) {
            throw new IllegalStateException("Cannot delete this category because there are active books assigned to it. Please reassign or delete the books first.");
        }

        category.setActive(false);
        categoryRepository.save(category);
    }
}