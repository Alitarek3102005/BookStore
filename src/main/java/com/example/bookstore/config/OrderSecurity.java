package com.example.bookstore.config;

import com.example.bookstore.domain.Order;
import com.example.bookstore.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component("orderSecurity")
@RequiredArgsConstructor
public class OrderSecurity {

    private final OrderRepository orderRepository;

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
}