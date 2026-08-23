package com.example.bookstore.controller;

import com.example.bookstore.dto.CartItemRequest;
import com.example.bookstore.dto.CartResponse;
import com.example.bookstore.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = CartController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CartService cartService;

    private UUID userId;
    private UUID bookId;
    private CartResponse cartResponse;
    private CartItemRequest cartItemRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        bookId = UUID.randomUUID();

        cartResponse = new CartResponse();
        cartResponse.setUserId(userId);
        cartResponse.setTotalAmount(40.0);

        cartItemRequest = new CartItemRequest();
        cartItemRequest.setBookId(bookId);
        cartItemRequest.setQuantity(2);
    }

    @Test
    void getAllCarts_ShouldReturn200Ok() throws Exception {
        when(cartService.getAllCarts()).thenReturn(List.of(cartResponse));

        mockMvc.perform(get("/api/carts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));

        verify(cartService).getAllCarts();
    }

    @Test
    void getOrCreateCart_ShouldReturn200Ok() throws Exception {
        when(cartService.getOrCreateCart(userId)).thenReturn(cartResponse);

        mockMvc.perform(get("/api/users/{userId}/cart", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()));

        verify(cartService).getOrCreateCart(userId);
    }

    @Test
    void addItemToCart_ShouldReturn200Ok() throws Exception {
        when(cartService.addItem(eq(userId), any(CartItemRequest.class))).thenReturn(cartResponse);

        mockMvc.perform(post("/api/users/{userId}/cart/items", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartItemRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(40.0));

        verify(cartService).addItem(eq(userId), any(CartItemRequest.class));
    }

    @Test
    void clearCart_ShouldReturn204NoContent() throws Exception {
        mockMvc.perform(delete("/api/users/{userId}/cart", userId))
                .andExpect(status().isNoContent());

        verify(cartService).clearCart(userId);
    }
}