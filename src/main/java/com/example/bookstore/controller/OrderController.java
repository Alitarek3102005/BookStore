package com.example.bookstore.controller;

import com.example.bookstore.api.OrdersApi;
import com.example.bookstore.dto.OrderPatchRequest;
import com.example.bookstore.dto.OrderRequest;
import com.example.bookstore.dto.OrderResponse;
import com.example.bookstore.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class OrderController implements OrdersApi {
    private final OrderService orderService;



    @Override
    public ResponseEntity<OrderResponse> createOrder(OrderRequest orderRequest) {
        return new ResponseEntity<>(orderService.Create(orderRequest), HttpStatus.CREATED);

    }

    @Override
    public ResponseEntity<Void> deleteOrder(UUID orderId) {
        orderService.Delete(orderId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }

    @Override
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return new ResponseEntity<>(orderService.getAllOrders(),HttpStatus.OK);

    }

    @Override
    public ResponseEntity<OrderResponse> getOrderById(UUID orderId) {
        return new ResponseEntity<>(orderService.getById(orderId),HttpStatus.OK);

    }

    @Override
    public ResponseEntity<List<OrderResponse>> getOrdersByUser(UUID userId) {
        return OrdersApi.super.getOrdersByUser(userId);
    }

    @Override
    public ResponseEntity<OrderResponse> patchOrder(UUID orderId, OrderPatchRequest orderPatchRequest) {
        return OrdersApi.super.patchOrder(orderId, orderPatchRequest);
    }

    @Override
    public ResponseEntity<OrderResponse> updateOrder(UUID orderId, OrderRequest orderRequest) {
        return new ResponseEntity<>(orderService.Update(orderId,orderRequest),HttpStatus.OK);

    }
}
