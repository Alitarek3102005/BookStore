package com.example.bookstore.service;

import com.example.bookstore.domain.OrderItem;
import com.example.bookstore.dto.OrderItemRequest;
import com.example.bookstore.dto.OrderItemResponse;
import com.example.bookstore.exception.OrderNotFoundException;
import com.example.bookstore.exception.BookNotFoundException;
import com.example.bookstore.exception.DuplicateResourceException;
import com.example.bookstore.mapper.OrderItemMapper;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.OrderItemRepository;
import com.example.bookstore.repository.OrderRepository;
import com.example.bookstore.dto.OrderItemPatchRequest;
import com.example.bookstore.exception.InvalidOrderException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;
    private final OrderItemMapper orderItemMapper;


    @Transactional(readOnly = true)
    public List<OrderItemResponse> findAll() {
        return orderItemRepository.findAll()
                .stream()
                .map(orderItemMapper::toResponse)
                .toList();
    }


    @Transactional(readOnly = true)
    public OrderItemResponse findById(UUID id) {
        OrderItem orderItem = orderItemRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order item not found: " + id));
        return orderItemMapper.toResponse(orderItem);
    }

    @Transactional
    public void delete(UUID id) {
        if (!orderItemRepository.existsById(id)) {
            throw new OrderNotFoundException("Order item not found: " + id);
        }
        orderItemRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<OrderItemResponse> findByOrderId(UUID orderId) {
        return orderItemRepository.findByOrderId(orderId)
                .stream()
                .map(orderItemMapper::toResponse)
                .toList();
    }

    @Transactional
    public OrderItemResponse save(OrderItemRequest request) {
        if (!orderRepository.existsById(request.getOrderId())) {
            throw new OrderNotFoundException("Order not found: " + request.getOrderId());
        }

        if (!bookRepository.existsById(request.getBookId())) {
            throw new BookNotFoundException("Book not found: " + request.getBookId());
        }

        if (orderItemRepository.existsByOrderIdAndBookId(request.getOrderId(), request.getBookId())) {
            throw new DuplicateResourceException(
                    "This book is already on the order - update its quantity instead");
        }

        OrderItem orderItem = orderItemMapper.toEntity(request);
        return orderItemMapper.toResponse(orderItemRepository.save(orderItem));
    }


    @Transactional
    public OrderItemResponse update(UUID id, OrderItemRequest request) {
        OrderItem existing = getOrThrow(id);

        if (!existing.getOrderId().equals(request.getOrderId())
                || !existing.getBookId().equals(request.getBookId())) {
            throw new InvalidOrderException(
                    "An order item cannot be moved to a different order or book");
        }

        orderItemMapper.updateEntityFromRequest(request, existing);
        return orderItemMapper.toResponse(orderItemRepository.save(existing));
    }

    @Transactional
    public OrderItemResponse patch(UUID id, OrderItemPatchRequest request) {
        OrderItem existing = getOrThrow(id);

        orderItemMapper.patchEntityFromRequest(request, existing);
        return orderItemMapper.toResponse(orderItemRepository.save(existing));
    }

    private OrderItem getOrThrow(UUID id) {
        return orderItemRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order item not found: " + id));
    }
}