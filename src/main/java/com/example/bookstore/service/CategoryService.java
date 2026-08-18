package com.example.bookstore.service;

import com.example.bookstore.domain.Category;
import com.example.bookstore.dto.CategoryPatchRequest;
import com.example.bookstore.dto.CategoryRequest;
import com.example.bookstore.dto.CategoryResponse;
import com.example.bookstore.exception.CategoryNotFoundException;
import com.example.bookstore.mapper.CategoryMapper;
import com.example.bookstore.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public List<CategoryResponse> findAll() {
        Iterable<Category> categories=categoryRepository.findAll();
        List<CategoryResponse> categoriesResponse=new ArrayList<>();
        for (Category category : categories) {
            categoriesResponse.add(categoryMapper.toResponse(category));
        }
        return categoriesResponse;
    }

    public CategoryResponse findById(UUID id) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category!=null) {
            return categoryMapper.toResponse(category);
        }else {
            throw new CategoryNotFoundException("Category not found");
        }
    }
    public CategoryResponse save(CategoryRequest category) {
        Category categoryEntity = categoryMapper.toEntity(category);
        categoryRepository.save(categoryEntity);
        return categoryMapper.toResponse(categoryEntity);
    }
    public CategoryResponse update(CategoryRequest category, UUID id) {
        Category categoryEntity =categoryRepository.findById(id).orElse(null);
        if (categoryEntity!=null) {
            categoryEntity=categoryMapper.toEntity(category);
            categoryRepository.save(categoryEntity);
            return categoryMapper.toResponse(categoryEntity);
        }else{
            throw new CategoryNotFoundException("Category not found");
        }
    }
    public void delete(UUID id) {
        categoryRepository.deleteById(id);
    }
    public CategoryResponse patch(UUID id, CategoryPatchRequest categoryPatchRequest) {
        Category categoryEntity = categoryRepository.findById(id).orElse(null);
        if (categoryEntity!=null) {
            categoryMapper.patchEntityFromRequest(categoryPatchRequest, categoryEntity);
            categoryRepository.save(categoryEntity);
            return categoryMapper.toResponse(categoryEntity);
        }else{
            throw new CategoryNotFoundException("Category not found");
        }
    }

}
