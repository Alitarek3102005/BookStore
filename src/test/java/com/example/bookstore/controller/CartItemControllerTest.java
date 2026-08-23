package com.example.bookstore.controller;

import com.example.bookstore.dto.CartItemPatchRequest;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = CartItemController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class CartItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CartService cartService;

    private UUID cartItemId;
    private CartItemPatchRequest patchRequest;
    private CartResponse cartResponse;

    @BeforeEach
    void setUp() {
        cartItemId = UUID.randomUUID();

        patchRequest = new CartItemPatchRequest();
        patchRequest.setQuantity(3);

        cartResponse = new CartResponse();
        cartResponse.setTotalAmount(45.0);
    }

    @Test
    void updateCartItemQuantity_ShouldReturn200Ok() throws Exception {
        when(cartService.updateItemQuantity(eq(cartItemId), any(CartItemPatchRequest.class)))
                .thenReturn(cartResponse);

        mockMvc.perform(patch("/api/cart-items/{cartItemId}", cartItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(45.0));

        verify(cartService).updateItemQuantity(eq(cartItemId), any(CartItemPatchRequest.class));
    }

    @Test
    void removeCartItem_ShouldReturn204NoContent() throws Exception {
        mockMvc.perform(delete("/api/cart-items/{cartItemId}", cartItemId))
                .andExpect(status().isNoContent());

        verify(cartService).removeItem(cartItemId);
    }
}