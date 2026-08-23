package com.example.bookstore.service;

import com.example.bookstore.domain.Book;
import com.example.bookstore.domain.Review;
import com.example.bookstore.domain.User;
import com.example.bookstore.dto.ReviewPatchRequest;
import com.example.bookstore.dto.ReviewRequest;
import com.example.bookstore.dto.ReviewResponse;
import com.example.bookstore.exception.BookNotFoundException;
import com.example.bookstore.exception.DuplicateResourceException;
import com.example.bookstore.exception.ReviewNotFoundException;
import com.example.bookstore.exception.UserNotFoundException;
import com.example.bookstore.mapper.ReviewMapper;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.ReviewRepository;
import com.example.bookstore.repository.UserRepository;
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
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

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
    public Page<ReviewResponse> searchReviews(UUID bookId, UUID userId, Integer rating, Integer page, Integer size, String sort) {
        if (bookId != null && !bookRepository.existsById(bookId)) {
            throw new BookNotFoundException("Book not found: " + bookId);
        }
        Pageable pageable = createPageable(page, size, sort);
        return reviewRepository.searchReviews(bookId, userId, rating, pageable)
                .map(reviewMapper::toResponse);
    }

    @Transactional
    public ReviewResponse createReview(UUID bookId, ReviewRequest request) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found: " + bookId));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + request.getUserId()));

        if (reviewRepository.existsByBook_IdAndCustomer_UserId(bookId, request.getUserId())) {
            throw new DuplicateResourceException("User has already reviewed this book.");
        }

        Review review = new Review();
        review.setBook(book);
        review.setCustomer(user);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        return reviewMapper.toResponse(reviewRepository.save(review));
    }

    @Transactional
    public ReviewResponse patchReview(UUID reviewId, ReviewPatchRequest request) {
        Review existing = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found: " + reviewId));

        if (request.getRating() != null) {
            existing.setRating(request.getRating());
        }
        if (request.getComment() != null) {
            existing.setComment(request.getComment());
        }

        return reviewMapper.toResponse(reviewRepository.save(existing));
    }

    @Transactional
    public void deleteReview(UUID reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new ReviewNotFoundException("Review not found: " + reviewId);
        }
        reviewRepository.deleteById(reviewId);
    }
}