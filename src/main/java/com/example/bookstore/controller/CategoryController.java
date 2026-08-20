package com.example.bookstore.controller;

import com.example.bookstore.api.CategoriesApi;
import com.example.bookstore.dto.BookResponse;
import com.example.bookstore.dto.CategoryPatchRequest;
import com.example.bookstore.dto.CategoryRequest;
import com.example.bookstore.dto.CategoryResponse;
import com.example.bookstore.service.BookService;
import com.example.bookstore.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CategoryController implements CategoriesApi {

    private final CategoryService categoryService;
    private final BookService bookService;

    @Override
    public ResponseEntity<CategoryResponse> createCategory(CategoryRequest categoryRequest) {
        return new ResponseEntity<>(categoryService.save(categoryRequest), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> deleteCategory(UUID categoryId) {
        categoryService.delete(categoryId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.findAll());
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
    public ResponseEntity<CategoryResponse> patchCategory(UUID categoryId, CategoryPatchRequest categoryPatchRequest) {
        return new ResponseEntity<>(categoryService.patch(categoryId, categoryPatchRequest), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CategoryResponse> updateCategory(UUID categoryId, CategoryRequest categoryRequest) {
        return new ResponseEntity<>(categoryService.update(categoryRequest, categoryId), HttpStatus.OK);
    }
}