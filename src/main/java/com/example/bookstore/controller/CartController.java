package com.example.bookstore.controller;

import com.example.bookstore.api.CartsApi;
import com.example.bookstore.dto.CartItemRequest;
import com.example.bookstore.dto.CartResponse;
import com.example.bookstore.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CartController implements CartsApi {

    private final CartService cartService;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CartResponse>> getAllCarts() {
        return ResponseEntity.ok(cartService.getAllCarts());
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or @ownershipSecurity.isSelf(authentication, #userId)")
    public ResponseEntity<CartResponse> getOrCreateCart(UUID userId) {
        return ResponseEntity.ok(cartService.getOrCreateCart(userId));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or @ownershipSecurity.isSelf(authentication, #userId)")
    public ResponseEntity<CartResponse> addItemToCart(UUID userId, CartItemRequest cartItemRequest) {
        return ResponseEntity.ok(cartService.addItem(userId, cartItemRequest));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or @ownershipSecurity.isSelf(authentication, #userId)")
    public ResponseEntity<Void> clearCart(UUID userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}