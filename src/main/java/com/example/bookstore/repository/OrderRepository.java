package com.example.bookstore.repository;

import com.example.bookstore.domain.Order;
import com.example.bookstore.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    // Reverted back to UserId
    Page<Order> findByCustomer_UserId(UUID userId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE " +
            "(:customerId IS NULL OR o.customer.userId = :customerId) AND " +
            "(:status IS NULL OR o.status = :status)")
    Page<Order> searchOrders(@Param("customerId") UUID customerId,
                             @Param("status") OrderStatus status,
                             Pageable pageable);
}