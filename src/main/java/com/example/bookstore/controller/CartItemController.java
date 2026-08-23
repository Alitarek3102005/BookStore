package com.example.bookstore.controller;

import com.example.bookstore.api.CartItemsApi; // Import the generated OpenAPI interface
import com.example.bookstore.dto.CartItemPatchRequest;
import com.example.bookstore.dto.CartResponse;
import com.example.bookstore.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CartItemController implements CartItemsApi {

    private final CartService cartService;

    @Override
    @PreAuthorize("hasRole('ADMIN') or @ownershipSecurity.isCartItemOwner(authentication, #cartItemId)")
    public ResponseEntity<CartResponse> updateCartItemQuantity(UUID cartItemId, CartItemPatchRequest cartItemPatchRequest) {
        return ResponseEntity.ok(cartService.updateItemQuantity(cartItemId, cartItemPatchRequest));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or @ownershipSecurity.isCartItemOwner(authentication, #cartItemId)")
    public ResponseEntity<Void> removeCartItem(UUID cartItemId) {
        cartService.removeItem(cartItemId);
        return ResponseEntity.noContent().build();
    }
}