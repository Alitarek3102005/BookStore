package com.example.bookstore.controller;

import com.example.bookstore.dto.ReviewPatchRequest;
import com.example.bookstore.dto.ReviewRequest;
import com.example.bookstore.dto.ReviewResponse;
import com.example.bookstore.service.ReviewService;
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
        controllers = ReviewController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ReviewService reviewService;

    private UUID bookId;
    private UUID reviewId;
    private ReviewRequest reviewRequest;
    private ReviewPatchRequest reviewPatchRequest;
    private ReviewResponse reviewResponse;

    @BeforeEach
    void setUp() {
        bookId = UUID.randomUUID();
        reviewId = UUID.randomUUID();

        reviewRequest = new ReviewRequest();
        reviewRequest.setUserId(UUID.randomUUID());
        reviewRequest.setRating(5);
        reviewRequest.setComment("Great!");

        reviewPatchRequest = new ReviewPatchRequest();
        reviewPatchRequest.setRating(4);

        reviewResponse = new ReviewResponse();
        reviewResponse.setReviewId(reviewId);
        reviewResponse.setRating(5);
    }

    @Test
    void getReviewsByBook_ShouldReturn200Ok() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        Page<ReviewResponse> reviewPage = new PageImpl<>(List.of(reviewResponse), pageable, 1);

        when(reviewService.searchReviews(eq(bookId), any(), any(), any(), any(), any()))
                .thenReturn(reviewPage);

        mockMvc.perform(get("/api/books/{bookId}/reviews", bookId))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].rating").value(5));

        verify(reviewService).searchReviews(eq(bookId), any(), any(), any(), any(), any());
    }

    @Test
    void createReview_ShouldReturn201Created() throws Exception {
        when(reviewService.createReview(eq(bookId), any(ReviewRequest.class))).thenReturn(reviewResponse);

        mockMvc.perform(post("/api/books/{bookId}/reviews", bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5));

        verify(reviewService).createReview(eq(bookId), any(ReviewRequest.class));
    }

    @Test
    void patchReview_ShouldReturn200Ok() throws Exception {
        when(reviewService.patchReview(eq(reviewId), any(ReviewPatchRequest.class))).thenReturn(reviewResponse);

        mockMvc.perform(patch("/api/reviews/{reviewId}", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewPatchRequest)))
                .andExpect(status().isOk());

        verify(reviewService).patchReview(eq(reviewId), any(ReviewPatchRequest.class));
    }

    @Test
    void deleteReview_ShouldReturn204NoContent() throws Exception {
        mockMvc.perform(delete("/api/reviews/{reviewId}", reviewId))
                .andExpect(status().isNoContent());

        verify(reviewService).deleteReview(reviewId);
    }
}