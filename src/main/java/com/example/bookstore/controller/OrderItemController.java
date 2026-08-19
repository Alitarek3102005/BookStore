package com.example.bookstore.controller;

import com.example.bookstore.api.OrderItemsApi;
import com.example.bookstore.dto.OrderItemPatchRequest;
import com.example.bookstore.dto.OrderItemRequest;
import com.example.bookstore.dto.OrderItemResponse;
import com.example.bookstore.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class OrderItemController implements OrderItemsApi {

    private final OrderItemService orderItemService;

    @Override
    public ResponseEntity<List<OrderItemResponse>> getAllOrderItems() {
        return ResponseEntity.ok(orderItemService.findAll());
    }

    @Override
    public ResponseEntity<OrderItemResponse> createOrderItem(OrderItemRequest orderItemRequest) {
        return new ResponseEntity<>(orderItemService.save(orderItemRequest), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<OrderItemResponse> getOrderItemById(UUID orderItemId) {
        return ResponseEntity.ok(orderItemService.findById(orderItemId));
    }

    @Override
    public ResponseEntity<OrderItemResponse> updateOrderItem(UUID orderItemId, OrderItemRequest orderItemRequest) {
        return ResponseEntity.ok(orderItemService.update(orderItemId, orderItemRequest));
    }

    @Override
    public ResponseEntity<OrderItemResponse> patchOrderItem(UUID orderItemId, OrderItemPatchRequest orderItemPatchRequest) {
        return ResponseEntity.ok(orderItemService.patch(orderItemId, orderItemPatchRequest));
    }

    @Override
    public ResponseEntity<Void> deleteOrderItem(UUID orderItemId) {
        orderItemService.delete(orderItemId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<List<OrderItemResponse>> getItemsByOrder(UUID orderId) {
        return ResponseEntity.ok(orderItemService.findByOrderId(orderId));
    }
}