package com.example.bookstore.controller;

import com.example.bookstore.api.CategoriesApi;
import com.example.bookstore.dto.BookResponse;
import com.example.bookstore.dto.CategoryPatchRequest;
import com.example.bookstore.dto.CategoryRequest;
import com.example.bookstore.dto.CategoryResponse;
import com.example.bookstore.service.BookService;
import com.example.bookstore.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CategoryController implements CategoriesApi {

    private final CategoryService categoryService;
    private final BookService bookService;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> createCategory(CategoryRequest categoryRequest) {
        return new ResponseEntity<>(categoryService.save(categoryRequest), HttpStatus.CREATED);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(UUID categoryId) {
        categoryService.delete(categoryId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<List<CategoryResponse>> getAllCategories(String keyword, Boolean active, Integer page, Integer size, String sort) {
        Page<CategoryResponse> categoryPage = categoryService.searchCategories(keyword, active, page, size, sort);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(categoryPage.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(categoryPage.getTotalPages()));

        return new ResponseEntity<>(categoryPage.getContent(), headers, HttpStatus.OK);
    }


    @Override
    public ResponseEntity<CategoryResponse> getCategoryById(UUID categoryId) {
        return new ResponseEntity<>(categoryService.findById(categoryId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<BookResponse>> getBooksByCategory(UUID categoryId) {
        return new ResponseEntity<>(bookService.getBooksByCategory(categoryId), HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> patchCategory(UUID categoryId, CategoryPatchRequest categoryPatchRequest) {
        return new ResponseEntity<>(categoryService.patch(categoryId, categoryPatchRequest), HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> updateCategory(UUID categoryId, CategoryRequest categoryRequest) {
        return new ResponseEntity<>(categoryService.update(categoryRequest, categoryId), HttpStatus.OK);
    }
}