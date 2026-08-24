package com.example.bookstore.repository;

import com.example.bookstore.domain.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    @Query("SELECT c FROM Category c WHERE " +
            "(:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))) AND " +
            "(:active IS NULL OR c.active = CAST(:active AS boolean))")
    Page<Category> searchCategories(@Param("keyword") String keyword,
                                    @Param("active") Boolean active,
                                    Pageable pageable);
}