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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    private UUID categoryId;
    private Category categoryEntity;
    private CategoryRequest categoryRequest;
    private CategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();

        categoryEntity = new Category();
        categoryEntity.setId(categoryId);
        categoryEntity.setName("Science Fiction");

        categoryRequest = new CategoryRequest();
        categoryRequest.setName("Science Fiction");

        categoryResponse = new CategoryResponse();
        categoryResponse.setCategoryId(categoryId);
        categoryResponse.setName("Science Fiction");
    }

    @Test
    void searchCategories_ShouldReturnPagedCategories() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Category> categoryPage = new PageImpl<>(List.of(categoryEntity), pageable, 1);

        when(categoryRepository.searchCategories(eq("Science"), any(Pageable.class)))
                .thenReturn(categoryPage);
        when(categoryMapper.toResponse(categoryEntity)).thenReturn(categoryResponse);

        Page<CategoryResponse> result = categoryService.searchCategories("Science", 0, 20, "name,asc");

        assertEquals(1, result.getTotalElements());
        assertEquals("Science Fiction", result.getContent().get(0).getName());
        verify(categoryRepository).searchCategories(eq("Science"), any(Pageable.class));
    }

    @Test
    void findById_ShouldReturnCategory_WhenExists() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(categoryEntity));
        when(categoryMapper.toResponse(categoryEntity)).thenReturn(categoryResponse);

        CategoryResponse result = categoryService.findById(categoryId);

        assertNotNull(result);
        assertEquals("Science Fiction", result.getName());
    }

    @Test
    void findById_ShouldThrowException_WhenDoesNotExist() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> categoryService.findById(categoryId));
    }

    @Test
    void save_ShouldSaveAndReturnCategory_WhenNameIsUnique() {
        when(categoryRepository.existsByNameIgnoreCase(categoryRequest.getName())).thenReturn(false);
        when(categoryMapper.toEntity(categoryRequest)).thenReturn(categoryEntity);
        when(categoryRepository.save(categoryEntity)).thenReturn(categoryEntity);
        when(categoryMapper.toResponse(categoryEntity)).thenReturn(categoryResponse);

        CategoryResponse result = categoryService.save(categoryRequest);

        assertNotNull(result);
        verify(categoryRepository).save(categoryEntity);
    }

    @Test
    void save_ShouldThrowException_WhenNameAlreadyExists() {
        when(categoryRepository.existsByNameIgnoreCase(categoryRequest.getName())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> categoryService.save(categoryRequest));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void update_ShouldUpdateAndSave_WhenValid() {
        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        when(categoryRepository.findByNameIgnoreCase(categoryRequest.getName())).thenReturn(Optional.empty());
        when(categoryMapper.toEntity(categoryRequest)).thenReturn(categoryEntity);
        when(categoryRepository.save(categoryEntity)).thenReturn(categoryEntity);
        when(categoryMapper.toResponse(categoryEntity)).thenReturn(categoryResponse);

        CategoryResponse result = categoryService.update(categoryRequest, categoryId);

        assertNotNull(result);
        assertEquals(categoryId, categoryEntity.getId());
        verify(categoryRepository).save(categoryEntity);
    }

    @Test
    void update_ShouldThrowException_WhenCategoryDoesNotExist() {
        when(categoryRepository.existsById(categoryId)).thenReturn(false);

        assertThrows(CategoryNotFoundException.class, () -> categoryService.update(categoryRequest, categoryId));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void update_ShouldThrowException_WhenNameBelongsToDifferentCategory() {
        when(categoryRepository.existsById(categoryId)).thenReturn(true);

        Category differentCategory = new Category();
        differentCategory.setId(UUID.randomUUID());
        differentCategory.setName(categoryRequest.getName());

        when(categoryRepository.findByNameIgnoreCase(categoryRequest.getName())).thenReturn(Optional.of(differentCategory));

        assertThrows(DuplicateResourceException.class, () -> categoryService.update(categoryRequest, categoryId));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void patch_ShouldUpdateAndSave_WhenNameIsUnique() {
        CategoryPatchRequest patchRequest = new CategoryPatchRequest();
        patchRequest.setName("Updated Fiction");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(categoryEntity));
        when(categoryRepository.findByNameIgnoreCase("Updated Fiction")).thenReturn(Optional.empty());
        when(categoryRepository.save(categoryEntity)).thenReturn(categoryEntity);
        when(categoryMapper.toResponse(categoryEntity)).thenReturn(categoryResponse);

        CategoryResponse result = categoryService.patch(categoryId, patchRequest);

        assertNotNull(result);
        verify(categoryMapper).patchEntityFromRequest(patchRequest, categoryEntity);
        verify(categoryRepository).save(categoryEntity);
    }

    @Test
    void patch_ShouldSkipNameCheck_WhenNameIsNull() {
        CategoryPatchRequest patchRequest = new CategoryPatchRequest();
        patchRequest.setName(null);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(categoryEntity));
        when(categoryRepository.save(categoryEntity)).thenReturn(categoryEntity);
        when(categoryMapper.toResponse(categoryEntity)).thenReturn(categoryResponse);

        CategoryResponse result = categoryService.patch(categoryId, patchRequest);

        assertNotNull(result);
        verify(categoryRepository, never()).findByNameIgnoreCase(anyString());
        verify(categoryRepository).save(categoryEntity);
    }

    @Test
    void patch_ShouldThrowException_WhenNameBelongsToDifferentCategory() {
        CategoryPatchRequest patchRequest = new CategoryPatchRequest();
        patchRequest.setName("Duplicate Name");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(categoryEntity));

        Category differentCategory = new Category();
        differentCategory.setId(UUID.randomUUID());

        when(categoryRepository.findByNameIgnoreCase("Duplicate Name")).thenReturn(Optional.of(differentCategory));

        assertThrows(DuplicateResourceException.class, () -> categoryService.patch(categoryId, patchRequest));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void delete_ShouldDelete_WhenCategoryExists() {
        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        when(bookRepository.existsByCategory_Id(categoryId)).thenReturn(false); // <-- Stubbed check

        categoryService.delete(categoryId);

        verify(categoryRepository).deleteById(categoryId);
    }

    @Test
    void delete_ShouldThrowException_WhenCategoryDoesNotExist() {
        when(categoryRepository.existsById(categoryId)).thenReturn(false);

        assertThrows(CategoryNotFoundException.class, () -> categoryService.delete(categoryId));
        verify(categoryRepository, never()).deleteById(any());
    }
}