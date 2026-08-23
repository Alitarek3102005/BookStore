package com.example.bookstore.repository;

import com.example.bookstore.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    Page<Review> findByBook_Id(UUID bookId, Pageable pageable);

    boolean existsByBook_IdAndCustomer_UserId(UUID bookId, UUID userId);

    void deleteByBook_Id(UUID bookId);

    @Query("SELECT r FROM Review r WHERE " +
            "(:bookId IS NULL OR r.book.id = :bookId) AND " +
            "(:userId IS NULL OR r.customer.userId = :userId) AND " +
            "(:rating IS NULL OR r.rating = :rating)")
    Page<Review> searchReviews(@Param("bookId") UUID bookId,
                               @Param("userId") UUID userId,
                               @Param("rating") Integer rating,
                               Pageable pageable);
}