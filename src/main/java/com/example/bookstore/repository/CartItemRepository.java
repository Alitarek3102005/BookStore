package com.example.bookstore.repository;

import com.example.bookstore.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    Optional<CartItem> findByCart_IdAndBook_Id(UUID cartId, UUID bookId);

    void deleteByBook_Id(UUID id);
}