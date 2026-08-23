package com.example.bookstore.controller;

import com.example.bookstore.dto.BookResponse;
import com.example.bookstore.dto.CategoryPatchRequest;
import com.example.bookstore.dto.CategoryRequest;
import com.example.bookstore.dto.CategoryResponse;
import com.example.bookstore.service.BookService;
import com.example.bookstore.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = CategoryController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private BookService bookService;

    private UUID categoryId;
    private CategoryRequest categoryRequest;
    private CategoryPatchRequest categoryPatchRequest;
    private CategoryResponse categoryResponse;
    private BookResponse bookResponse;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();

        categoryRequest = new CategoryRequest();
        categoryRequest.setName("Science Fiction");

        categoryPatchRequest = new CategoryPatchRequest();
        categoryPatchRequest.setName("Epic Fantasy");

        categoryResponse = new CategoryResponse();
        categoryResponse.setCategoryId(categoryId);
        categoryResponse.setName("Science Fiction");

        bookResponse = new BookResponse();
        bookResponse.setBookId(UUID.randomUUID());
        bookResponse.setTitle("Dune");
    }

    @Test
    void createCategory_ShouldReturn201Created() throws Exception {
        when(categoryService.save(any(CategoryRequest.class))).thenReturn(categoryResponse);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryId").value(categoryId.toString()))
                .andExpect(jsonPath("$.name").value("Science Fiction"));

        verify(categoryService).save(any(CategoryRequest.class));
    }

    @Test
    void getAllCategories_ShouldReturn200Ok() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        Page<CategoryResponse> categoryPage = new PageImpl<>(List.of(categoryResponse), pageable, 1);

        when(categoryService.searchCategories(any(), any(), any(), any()))
                .thenReturn(categoryPage);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("Science Fiction"));

        verify(categoryService).searchCategories(any(), any(), any(), any());
    }

    @Test
    void getCategoryById_ShouldReturn200Ok() throws Exception {
        when(categoryService.findById(categoryId)).thenReturn(categoryResponse);

        mockMvc.perform(get("/api/categories/{categoryId}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(categoryId.toString()))
                .andExpect(jsonPath("$.name").value("Science Fiction"));

        verify(categoryService).findById(categoryId);
    }

    @Test
    void getBooksByCategory_ShouldReturn200Ok() throws Exception {
        when(bookService.getBooksByCategory(categoryId)).thenReturn(List.of(bookResponse));

        mockMvc.perform(get("/api/categories/{categoryId}/books", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].title").value("Dune"));

        verify(bookService).getBooksByCategory(categoryId);
    }

    @Test
    void updateCategory_ShouldReturn200Ok() throws Exception {
        when(categoryService.update(any(CategoryRequest.class), eq(categoryId))).thenReturn(categoryResponse);

        mockMvc.perform(put("/api/categories/{categoryId}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Science Fiction"));

        verify(categoryService).update(any(CategoryRequest.class), eq(categoryId));
    }

    @Test
    void patchCategory_ShouldReturn200Ok() throws Exception {
        when(categoryService.patch(eq(categoryId), any(CategoryPatchRequest.class))).thenReturn(categoryResponse);

        mockMvc.perform(patch("/api/categories/{categoryId}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryPatchRequest)))
                .andExpect(status().isOk());

        verify(categoryService).patch(eq(categoryId), any(CategoryPatchRequest.class));
    }

    @Test
    void deleteCategory_ShouldReturn204NoContent() throws Exception {
        mockMvc.perform(delete("/api/categories/{categoryId}", categoryId))
                .andExpect(status().isNoContent());

        verify(categoryService).delete(categoryId);
    }
}