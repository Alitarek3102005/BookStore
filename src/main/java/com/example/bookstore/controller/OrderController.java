package com.example.bookstore.controller;

import com.example.bookstore.api.OrdersApi;
import com.example.bookstore.dto.OrderPatchRequest;
import com.example.bookstore.dto.OrderRequest;
import com.example.bookstore.dto.OrderResponse;
import com.example.bookstore.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class OrderController implements OrdersApi {

    private final OrderService orderService;

    @Override
    @PreAuthorize("hasRole('ADMIN') or @ownershipSecurity.isSelf(authentication, #orderRequest.userId)")
    public ResponseEntity<OrderResponse> createOrder(OrderRequest orderRequest) {
        return new ResponseEntity<>(orderService.createOrder(orderRequest), HttpStatus.CREATED);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOrder(UUID orderId) {
        orderService.delete(orderId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return new ResponseEntity<>(orderService.getAllOrders(), HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or @ownershipSecurity.isOrderOwner(authentication, #orderId)")
    public ResponseEntity<OrderResponse> getOrderById(UUID orderId) {
        return new ResponseEntity<>(orderService.getById(orderId), HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or @ownershipSecurity.isSelf(authentication, #userId)")
    public ResponseEntity<List<OrderResponse>> getOrdersByUser(UUID userId) {
        return new ResponseEntity<>(orderService.getOrdersByUser(userId), HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> patchOrder(UUID orderId, OrderPatchRequest orderPatchRequest) {
        return new ResponseEntity<>(orderService.patch(orderId, orderPatchRequest), HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or @ownershipSecurity.isOrderOwner(authentication, #orderId)")
    public ResponseEntity<OrderResponse> payOrder(UUID orderId) {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        OrderResponse response = orderService.payOrder(orderId, jwt);
        return ResponseEntity.ok(response);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateOrder(UUID orderId, OrderRequest orderRequest) {
        return new ResponseEntity<>(orderService.update(orderId, orderRequest), HttpStatus.OK);
    }
}