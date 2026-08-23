package com.example.bookstore.controller;

import com.example.bookstore.dto.OrderItemLine;
import com.example.bookstore.dto.OrderPatchRequest;
import com.example.bookstore.dto.OrderRequest;
import com.example.bookstore.dto.OrderResponse;
import com.example.bookstore.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = OrderController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private OrderService orderService;

    private UUID orderId;
    private UUID userId;
    private UUID bookId;
    private OrderRequest orderRequest;
    private OrderPatchRequest orderPatchRequest;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        userId = UUID.randomUUID();
        bookId = UUID.randomUUID();

        OrderItemLine itemLine = new OrderItemLine();
        itemLine.setBookId(bookId);
        itemLine.setQuantity(2);

        orderRequest = new OrderRequest();
        orderRequest.setUserId(userId);
        orderRequest.setItems(Collections.singletonList(itemLine));

        orderPatchRequest = new OrderPatchRequest();
        orderPatchRequest.setStatus(OrderPatchRequest.StatusEnum.SHIPPED);

        orderResponse = new OrderResponse();
        orderResponse.setOrderId(orderId);
        orderResponse.setTotalAmount(50.0);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createOrder_ShouldReturn201Created() throws Exception {
        when(orderService.createOrder(any(OrderRequest.class))).thenReturn(orderResponse);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.totalAmount").value(50.0));

        verify(orderService).createOrder(any(OrderRequest.class));
    }

    @Test
    void getAllOrders_ShouldReturn200Ok() throws Exception {
        when(orderService.getAllOrders()).thenReturn(List.of(orderResponse));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].orderId").value(orderId.toString()));

        verify(orderService).getAllOrders();
    }

    @Test
    void getOrderById_ShouldReturn200Ok() throws Exception {
        when(orderService.getById(orderId)).thenReturn(orderResponse);

        mockMvc.perform(get("/api/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()));

        verify(orderService).getById(orderId);
    }

    @Test
    void getOrdersByUser_ShouldReturn200Ok() throws Exception {
        when(orderService.getOrdersByUser(userId)).thenReturn(List.of(orderResponse));

        mockMvc.perform(get("/api/users/{userId}/orders", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));

        verify(orderService).getOrdersByUser(userId);
    }

    @Test
    void updateOrder_ShouldReturn200Ok() throws Exception {
        when(orderService.update(eq(orderId), any(OrderRequest.class))).thenReturn(orderResponse);

        mockMvc.perform(put("/api/orders/{orderId}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk());

        verify(orderService).update(eq(orderId), any(OrderRequest.class));
    }

    @Test
    void patchOrder_ShouldReturn200Ok() throws Exception {
        when(orderService.patch(eq(orderId), any(OrderPatchRequest.class))).thenReturn(orderResponse);

        mockMvc.perform(patch("/api/orders/{orderId}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderPatchRequest)))
                .andExpect(status().isOk());

        verify(orderService).patch(eq(orderId), any(OrderPatchRequest.class));
    }

    @Test
    void deleteOrder_ShouldReturn204NoContent() throws Exception {
        mockMvc.perform(delete("/api/orders/{orderId}", orderId))
                .andExpect(status().isNoContent());

        verify(orderService).delete(orderId);
    }

    @Test
    void payOrder_ShouldReturn200Ok() throws Exception {
        Jwt mockJwt = mock(Jwt.class);
        Authentication mockAuthentication = mock(Authentication.class);
        SecurityContext mockSecurityContext = mock(SecurityContext.class);

        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        SecurityContextHolder.setContext(mockSecurityContext);

        when(orderService.payOrder(eq(orderId), eq(mockJwt))).thenReturn(orderResponse);

        mockMvc.perform(post("/api/orders/{orderId}/pay", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.totalAmount").value(50.0));

        verify(orderService).payOrder(eq(orderId), eq(mockJwt));
    }
}