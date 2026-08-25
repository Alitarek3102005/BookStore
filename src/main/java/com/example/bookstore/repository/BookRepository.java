package com.example.bookstore.repository;

import com.example.bookstore.domain.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {

    @Query("SELECT b FROM Book b WHERE " +
            "(:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', CAST(:title AS string), '%'))) AND " +
            "(:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', CAST(:author AS string), '%'))) AND " +
            "(:categoryId IS NULL OR b.category.id = :categoryId) AND " +
            "(:active IS NULL OR b.active = :active)")
    Page<Book> searchBooks(@Param("title") String title,
                           @Param("author") String author,
                           @Param("categoryId") UUID categoryId,
                           @Param("active") Boolean active,
                           Pageable pageable);
    List<Book> findByCategory_Id(UUID categoryId);

    boolean existsByCategory_Id(UUID categoryId);
}