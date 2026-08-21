package com.example.bookstore.controller;

import com.example.bookstore.dto.OrderItemPatchRequest;
import com.example.bookstore.dto.OrderItemRequest;
import com.example.bookstore.dto.OrderItemResponse;
import com.example.bookstore.service.OrderItemService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = OrderItemController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class OrderItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private OrderItemService orderItemService;

    private UUID orderItemId;
    private UUID orderId;
    private UUID bookId;
    private OrderItemRequest orderItemRequest;
    private OrderItemPatchRequest orderItemPatchRequest;
    private OrderItemResponse orderItemResponse;

    @BeforeEach
    void setUp() {
        orderItemId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        bookId = UUID.randomUUID();

        orderItemRequest = new OrderItemRequest();
        orderItemRequest.setOrderId(orderId);
        orderItemRequest.setBookId(bookId);
        orderItemRequest.setQuantity(3);
        orderItemRequest.setUnitPrice(19.99);

        orderItemPatchRequest = new OrderItemPatchRequest();
        orderItemPatchRequest.setQuantity(5);

        orderItemResponse = new OrderItemResponse();
        orderItemResponse.setOrderItemId(orderItemId);
        orderItemResponse.setQuantity(3);
    }

    @Test
    void createOrderItem_ShouldReturn201Created() throws Exception {
        when(orderItemService.save(any(OrderItemRequest.class))).thenReturn(orderItemResponse);

        mockMvc.perform(post("/api/order-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderItemRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderItemId").value(orderItemId.toString()))
                .andExpect(jsonPath("$.quantity").value(3));

        verify(orderItemService).save(any(OrderItemRequest.class));
    }

    @Test
    void getAllOrderItems_ShouldReturn200Ok() throws Exception {
        when(orderItemService.findAll()).thenReturn(List.of(orderItemResponse));

        mockMvc.perform(get("/api/order-items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].orderItemId").value(orderItemId.toString()));

        verify(orderItemService).findAll();
    }

    @Test
    void getOrderItemById_ShouldReturn200Ok() throws Exception {
        when(orderItemService.findById(orderItemId)).thenReturn(orderItemResponse);

        mockMvc.perform(get("/api/order-items/{orderItemId}", orderItemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderItemId").value(orderItemId.toString()));

        verify(orderItemService).findById(orderItemId);
    }

    @Test
    void getItemsByOrder_ShouldReturn200Ok() throws Exception {
        when(orderItemService.findByOrderId(orderId)).thenReturn(List.of(orderItemResponse));

        // Fixed: Updated to match OpenAPI spec path: /api/orders/{orderId}/items
        mockMvc.perform(get("/api/orders/{orderId}/items", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));

        verify(orderItemService).findByOrderId(orderId);
    }

    @Test
    void updateOrderItem_ShouldReturn200Ok() throws Exception {
        when(orderItemService.update(eq(orderItemId), any(OrderItemRequest.class))).thenReturn(orderItemResponse);

        mockMvc.perform(put("/api/order-items/{orderItemId}", orderItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderItemRequest)))
                .andExpect(status().isOk());

        verify(orderItemService).update(eq(orderItemId), any(OrderItemRequest.class));
    }

    @Test
    void patchOrderItem_ShouldReturn200Ok() throws Exception {
        when(orderItemService.patch(eq(orderItemId), any(OrderItemPatchRequest.class))).thenReturn(orderItemResponse);

        mockMvc.perform(patch("/api/order-items/{orderItemId}", orderItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderItemPatchRequest)))
                .andExpect(status().isOk());

        verify(orderItemService).patch(eq(orderItemId), any(OrderItemPatchRequest.class));
    }

    @Test
    void deleteOrderItem_ShouldReturn204NoContent() throws Exception {
        mockMvc.perform(delete("/api/order-items/{orderItemId}", orderItemId))
                .andExpect(status().isNoContent());

        verify(orderItemService).delete(orderItemId);
    }
}