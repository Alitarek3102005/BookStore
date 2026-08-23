package com.example.bookstore.service;

import com.example.bookstore.domain.Book;
import com.example.bookstore.domain.Review;
import com.example.bookstore.domain.User;
import com.example.bookstore.dto.ReviewPatchRequest;
import com.example.bookstore.dto.ReviewRequest;
import com.example.bookstore.dto.ReviewResponse;
import com.example.bookstore.exception.BookNotFoundException;
import com.example.bookstore.exception.DuplicateResourceException;
import com.example.bookstore.exception.ResourceNotFoundException;
import com.example.bookstore.mapper.ReviewMapper;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.ReviewRepository;
import com.example.bookstore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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
@MockitoSettings(strictness = Strictness.LENIENT)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewService reviewService;

    private UUID reviewId;
    private UUID bookId;
    private UUID userId;
    private Review reviewEntity;
    private ReviewRequest reviewRequest;
    private ReviewResponse reviewResponse;

    @BeforeEach
    void setUp() {
        reviewId = UUID.randomUUID();
        bookId = UUID.randomUUID();
        userId = UUID.randomUUID();

        reviewEntity = new Review();
        reviewEntity.setId(reviewId);
        reviewEntity.setRating(5);

        reviewRequest = new ReviewRequest();
        reviewRequest.setUserId(userId);
        reviewRequest.setRating(5);
        reviewRequest.setComment("Amazing book!");

        reviewResponse = new ReviewResponse();
        reviewResponse.setReviewId(reviewId);
        reviewResponse.setRating(5);

        // Stub existence checks to default true globally
        lenient().when(bookRepository.existsById(any(UUID.class))).thenReturn(true);
        lenient().when(bookRepository.findById(any(UUID.class))).thenReturn(Optional.of(new Book()));
        lenient().when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(new User()));
    }

    @Test
    void searchReviews_ShouldReturnPagedReviews() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Review> reviewPage = new PageImpl<>(List.of(reviewEntity), pageable, 1);

        when(reviewRepository.searchReviews(eq(bookId), eq(userId), eq(5), any(Pageable.class)))
                .thenReturn(reviewPage);
        when(reviewMapper.toResponse(reviewEntity)).thenReturn(reviewResponse);

        Page<ReviewResponse> result = reviewService.searchReviews(bookId, userId, 5, 0, 20, "createdAt,desc");

        assertEquals(1, result.getTotalElements());
        verify(reviewRepository).searchReviews(eq(bookId), eq(userId), eq(5), any(Pageable.class));
    }

    @Test
    void searchReviews_ShouldThrowException_WhenBookNotFound() {
        // Stub existsById to return false for the not-found check
        when(bookRepository.existsById(bookId)).thenReturn(false);

        assertThrows(BookNotFoundException.class, () -> reviewService.searchReviews(bookId, userId, 5, 0, 20, "createdAt,desc"));
        verify(reviewRepository, never()).searchReviews(any(), any(), any(), any());
    }

    @Test
    void createReview_ShouldSaveAndReturnReview_WhenValid() {
        when(reviewRepository.existsByBook_IdAndCustomer_UserId(bookId, userId)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(reviewEntity);
        when(reviewMapper.toResponse(reviewEntity)).thenReturn(reviewResponse);

        ReviewResponse result = reviewService.createReview(bookId, reviewRequest);

        assertNotNull(result);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void createReview_ShouldThrowException_WhenAlreadyReviewed() {
        when(reviewRepository.existsByBook_IdAndCustomer_UserId(bookId, userId)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> reviewService.createReview(bookId, reviewRequest));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void patchReview_ShouldUpdateAndReturn_WhenExists() {
        ReviewPatchRequest patchRequest = new ReviewPatchRequest();
        patchRequest.setRating(4);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(reviewEntity));
        when(reviewRepository.save(reviewEntity)).thenReturn(reviewEntity);
        when(reviewMapper.toResponse(reviewEntity)).thenReturn(reviewResponse);

        ReviewResponse result = reviewService.patchReview(reviewId, patchRequest);

        assertNotNull(result);
        verify(reviewRepository).save(reviewEntity);
    }

    @Test
    void deleteReview_ShouldDelete_WhenExists() {
        when(reviewRepository.existsById(reviewId)).thenReturn(true);

        reviewService.deleteReview(reviewId);

        verify(reviewRepository).deleteById(reviewId);
    }

    @Test
    void deleteReview_ShouldThrowException_WhenNotFound() {
        when(reviewRepository.existsById(reviewId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> reviewService.deleteReview(reviewId));
        verify(reviewRepository, never()).deleteById(any());
    }
}