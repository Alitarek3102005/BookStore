package com.example.bookstore.controller;

import com.example.bookstore.api.ReviewsApi;
import com.example.bookstore.dto.ReviewPatchRequest;
import com.example.bookstore.dto.ReviewRequest;
import com.example.bookstore.dto.ReviewResponse;
import com.example.bookstore.service.ReviewService;
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
public class ReviewController implements ReviewsApi {

    private final ReviewService reviewService;

    @Override
    public ResponseEntity<List<ReviewResponse>> getReviewsByBook(UUID bookId, UUID userId, Integer rating, Integer page, Integer size, String sort) {
        Page<ReviewResponse> reviewPage = reviewService.searchReviews(bookId, userId, rating, page, size, sort);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(reviewPage.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(reviewPage.getTotalPages()));

        return new ResponseEntity<>(reviewPage.getContent(), headers, HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or @ownershipSecurity.isSelf(authentication, #reviewRequest.userId)")
    public ResponseEntity<ReviewResponse> createReview(UUID bookId, ReviewRequest reviewRequest) {
        ReviewResponse createdReview = reviewService.createReview(bookId, reviewRequest);
        return new ResponseEntity<>(createdReview, HttpStatus.CREATED);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or @ownershipSecurity.isReviewOwner(authentication, #reviewId)")
    public ResponseEntity<ReviewResponse> patchReview(UUID reviewId, ReviewPatchRequest reviewPatchRequest) {
        return ResponseEntity.ok(reviewService.patchReview(reviewId, reviewPatchRequest));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or @ownershipSecurity.isReviewOwner(authentication, #reviewId)")
    public ResponseEntity<Void> deleteReview(UUID reviewId) {
        reviewService.deleteReview(reviewId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}