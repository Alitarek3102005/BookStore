package com.example.bookstore.config;

import com.example.bookstore.domain.Order;
import com.example.bookstore.repository.CartItemRepository;
import com.example.bookstore.repository.OrderRepository;
import com.example.bookstore.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component("ownershipSecurity")
@RequiredArgsConstructor
public class OwnershipSecurity {

    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final CartItemRepository cartItemRepository;

    public boolean isOrderOwner(Authentication authentication, UUID orderId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            String tokenUserId = jwt.getSubject();

            Optional<Order> orderOptional = orderRepository.findById(orderId);

            if (orderOptional.isPresent()) {
                Order order = orderOptional.get();
                return order.getCustomer().getUserId().toString().equals(tokenUserId);
            }
        }

        return false;
    }
    public boolean isSelf(Authentication authentication, UUID userId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            String tokenUserId = jwt.getSubject();
            return tokenUserId.equals(userId.toString());
        }

        return false;
    }
    public boolean isReviewOwner(Authentication authentication, UUID reviewId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            String tokenUserId = jwt.getSubject();
            return reviewRepository.findById(reviewId)
                    .map(review -> review.getCustomer().getUserId().toString().equals(tokenUserId))
                    .orElse(false);
        }
        return false;
    }
    public boolean isCartItemOwner(Authentication authentication, UUID cartItemId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            String tokenUserId = jwt.getSubject();
            return cartItemRepository.findById(cartItemId)
                    .map(item -> item.getCart().getCustomer().getUserId().toString().equals(tokenUserId))
                    .orElse(false);
        }
        return false;
    }
}