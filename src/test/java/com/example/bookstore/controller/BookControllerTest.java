package com.example.bookstore.controller;

import com.example.bookstore.dto.BookPatchRequest;
import com.example.bookstore.dto.BookRequest;
import com.example.bookstore.dto.BookResponse;
import com.example.bookstore.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
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
        controllers = BookController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private BookService bookService;

    private UUID bookId;
    private UUID categoryId;
    private BookRequest bookRequest;
    private BookPatchRequest bookPatchRequest;
    private BookResponse bookResponse;

    @BeforeEach
    void setUp() {
        bookId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        bookRequest = new BookRequest();
        bookRequest.setTitle("Dune");
        bookRequest.setAuthor("Frank Herbert");
        bookRequest.setQuantity(10);
        bookRequest.setCategoryId(categoryId);
        bookRequest.setPrice(29.99);
        bookRequest.setActive(true);

        bookPatchRequest = new BookPatchRequest();
        bookPatchRequest.setTitle("Dune: Messiah");

        bookResponse = new BookResponse();
        bookResponse.setBookId(bookId);
        bookResponse.setTitle("Dune");
        bookResponse.setActive(true);
    }

    @Test
    void createBook_ShouldReturn201Created() throws Exception {
        when(bookService.addBook(any(BookRequest.class))).thenReturn(bookResponse);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookId").value(bookId.toString()))
                .andExpect(jsonPath("$.title").value("Dune"));

        verify(bookService).addBook(any(BookRequest.class));
    }

    @Test
    void getAllBooks_ShouldReturn200OkWithParameters() throws Exception {
        when(bookService.searchBooks(any(), any(), any(), any(), any(), any(), any())).thenReturn(List.of(bookResponse));

        mockMvc.perform(get("/api/books")
                        .param("title", "Dune")
                        .param("active", "true")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].title").value("Dune"));

        verify(bookService).searchBooks(eq("Dune"), any(), any(), eq(true), eq(0), eq(10), any());
    }

    @Test
    void getBookById_ShouldReturn200Ok() throws Exception {
        when(bookService.getById(bookId)).thenReturn(bookResponse);

        mockMvc.perform(get("/api/books/{bookId}", bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(bookId.toString()));

        verify(bookService).getById(bookId);
    }

    @Test
    void updateBook_ShouldReturn200Ok() throws Exception {
        when(bookService.updateBook(eq(bookId), any(BookRequest.class))).thenReturn(bookResponse);

        mockMvc.perform(put("/api/books/{bookId}", bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Dune"));

        verify(bookService).updateBook(eq(bookId), any(BookRequest.class));
    }

    @Test
    void patchBook_ShouldReturn200Ok() throws Exception {
        when(bookService.patchBook(eq(bookId), any(BookPatchRequest.class))).thenReturn(bookResponse);

        mockMvc.perform(patch("/api/books/{bookId}", bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookPatchRequest)))
                .andExpect(status().isOk());

        verify(bookService).patchBook(eq(bookId), any(BookPatchRequest.class));
    }

    @Test
    void deleteBook_ShouldReturn204NoContent() throws Exception {
        mockMvc.perform(delete("/api/books/{bookId}", bookId))
                .andExpect(status().isNoContent());

        verify(bookService).deleteBook(bookId);
    }
}