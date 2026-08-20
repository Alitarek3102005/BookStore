package com.example.bookstore.service;

import com.example.bookstore.domain.Book;
import com.example.bookstore.domain.Order;
import com.example.bookstore.domain.OrderItem;
import com.example.bookstore.domain.User;
import com.example.bookstore.dto.OrderPatchRequest;
import com.example.bookstore.dto.OrderRequest;
import com.example.bookstore.dto.OrderResponse;
import com.example.bookstore.exception.BookNotFoundException;
import com.example.bookstore.exception.OrderNotFoundException;
import com.example.bookstore.exception.UserNotFoundException;
import com.example.bookstore.mapper.OrderMapper;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.OrderRepository;
import com.example.bookstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final OrderMapper orderMapper;

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));
        return orderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found: " + userId);
        }
        return orderRepository.findByCustomer_UserId(userId).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        User customer = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + request.getUserId()));

        Order order = orderMapper.toEntity(request);
        order.setCustomer(customer);

        List<OrderItem> orderItems = new ArrayList<>();
        double totalPrice = 0.0;

        for (var itemRequest : request.getItems()) {
            Book book = bookRepository.findById(itemRequest.getBookId())
                    .orElseThrow(() -> new BookNotFoundException("Book not found: " + itemRequest.getBookId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setBook(book);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(book.getPrice());

            totalPrice += (book.getPrice().doubleValue() * itemRequest.getQuantity());
            orderItems.add(orderItem);
        }

        order.setOrderItems(orderItems);
        order.setTotalPrice(totalPrice);

        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse update(UUID id, OrderRequest request) {
        Order existing = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));

        orderMapper.updateEntityFromRequest(request, existing);
        return orderMapper.toResponse(orderRepository.save(existing));
    }

    @Transactional
    public OrderResponse patch(UUID id, OrderPatchRequest request) {
        Order existing = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));

        if (request.getStatus() != null) {
            existing.setStatus(com.example.bookstore.domain.OrderStatus.valueOf(request.getStatus().getValue()));
        }

        return orderMapper.toResponse(orderRepository.save(existing));
    }

    @Transactional
    public void delete(UUID id) {
        if (!orderRepository.existsById(id)) {
            throw new OrderNotFoundException("Order not found: " + id);
        }
        orderRepository.deleteById(id);
    }
}