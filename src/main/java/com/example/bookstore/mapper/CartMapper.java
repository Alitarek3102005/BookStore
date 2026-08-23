package com.example.bookstore.mapper;

import com.example.bookstore.domain.Cart;
import com.example.bookstore.dto.CartResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring", uses = {CartItemMapper.class})
public interface CartMapper {

    @Mapping(target = "cartId", source = "id")
    @Mapping(target = "userId", source = "customer.userId")
    @Mapping(target = "items", source = "cartItems")
    @Mapping(target = "totalAmount", expression = "java(computeTotal(cart))")
    CartResponse toResponse(Cart cart);

    default double computeTotal(Cart cart) {
        if (cart.getCartItems() == null) {
            return 0.0;
        }
        return cart.getCartItems().stream()
                .mapToDouble(item -> item.getBook().getPrice().doubleValue() * item.getQuantity())
                .sum();
    }

    default OffsetDateTime map(LocalDateTime value) {
        return value == null ? null : value.atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }
}