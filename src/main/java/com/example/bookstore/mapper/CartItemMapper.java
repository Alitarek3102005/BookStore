package com.example.bookstore.mapper;

import com.example.bookstore.domain.CartItem;
import com.example.bookstore.dto.CartItemResponse;
import org.mapstruct.Mapper;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface CartItemMapper {
    default CartItemResponse toResponse(CartItem cartItem) {
        if (cartItem == null) {
            return null;
        }
        BigDecimal unitPrice = cartItem.getBook().getPrice();
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        CartItemResponse response = new CartItemResponse();
        response.setCartItemId(cartItem.getId());
        response.setBookId(cartItem.getBook().getId());
        response.setTitle(cartItem.getBook().getTitle());
        response.setQuantity(cartItem.getQuantity());
        response.setUnitPrice(unitPrice.doubleValue());
        response.setSubtotal(subtotal.doubleValue());
        return response;
    }
}