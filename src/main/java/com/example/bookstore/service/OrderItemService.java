package com.example.bookstore.service;

import com.example.bookstore.domain.Book;
import com.example.bookstore.domain.Order;
import com.example.bookstore.domain.OrderItem;
import com.example.bookstore.domain.OrderStatus;
import com.example.bookstore.dto.OrderItemPatchRequest;
import com.example.bookstore.dto.OrderItemRequest;
import com.example.bookstore.dto.OrderItemResponse;
import com.example.bookstore.exception.BookNotFoundException;
import com.example.bookstore.exception.DuplicateResourceException;
import com.example.bookstore.exception.InsufficientStockException;
import com.example.bookstore.exception.InvalidOrderException;
import com.example.bookstore.exception.OrderNotFoundException;
import com.example.bookstore.mapper.OrderItemMapper;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.OrderItemRepository;
import com.example.bookstore.repository.OrderRepository;
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
        return orderItemRepository.findAll().stream()
                .map(orderItemMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderItemResponse findById(UUID id) {
        return orderItemMapper.toResponse(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<OrderItemResponse> findByOrderId(UUID orderId) {
        return orderItemRepository.findByOrder_Id(orderId).stream()
                .map(orderItemMapper::toResponse)
                .toList();
    }

    @Transactional
    public OrderItemResponse save(OrderItemRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + request.getOrderId()));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderException("Cannot add items to an order that is not PENDING.");
        }

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new BookNotFoundException("Book not found: " + request.getBookId()));

        if (orderItemRepository.existsByOrder_IdAndBook_Id(request.getOrderId(), request.getBookId())) {
            throw new DuplicateResourceException("This book is already on the order - update its quantity instead");
        }

        if (book.getQuantity() < request.getQuantity()) {
            throw new InsufficientStockException("Not enough stock for book: " + book.getTitle());
        }

        book.setQuantity(book.getQuantity() - request.getQuantity());
        bookRepository.save(book);

        OrderItem orderItem = orderItemMapper.toEntity(request);
        orderItem.setOrder(order);
        orderItem.setBook(book);
        orderItem.setUnitPrice(book.getPrice());

        double lineTotal = book.getPrice().doubleValue() * request.getQuantity();
        order.setTotalPrice(order.getTotalPrice() + lineTotal);
        orderRepository.save(order);

        return orderItemMapper.toResponse(orderItemRepository.save(orderItem));
    }

    @Transactional
    public OrderItemResponse update(UUID id, OrderItemRequest request) {
        OrderItem existing = getOrThrow(id);
        Order order = existing.getOrder();

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderException("Cannot update items on an order that is not PENDING.");
        }
        if (!existing.getOrder().getId().equals(request.getOrderId())
                || !existing.getBook().getId().equals(request.getBookId())) {
            throw new InvalidOrderException("An order item cannot be moved to a different order or book");
        }

        Book book = existing.getBook();
        int quantityDifference = request.getQuantity() - existing.getQuantity();

        if (quantityDifference > 0 && book.getQuantity() < quantityDifference) {
            throw new InsufficientStockException("Not enough stock to increase quantity.");
        }

        book.setQuantity(book.getQuantity() - quantityDifference);
        bookRepository.save(book);

        double priceDifference = existing.getUnitPrice().doubleValue() * quantityDifference;
        order.setTotalPrice(order.getTotalPrice() + priceDifference);
        orderRepository.save(order);

        orderItemMapper.updateEntityFromRequest(request, existing);
        return orderItemMapper.toResponse(orderItemRepository.save(existing));
    }

    @Transactional
    public OrderItemResponse patch(UUID id, OrderItemPatchRequest request) {
        OrderItem existing = getOrThrow(id);
        Order order = existing.getOrder();

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderException("Cannot patch items on an order that is not PENDING.");
        }

        if (request.getQuantity() != null) {
            Book book = existing.getBook();
            int quantityDifference = request.getQuantity() - existing.getQuantity();

            if (quantityDifference > 0 && book.getQuantity() < quantityDifference) {
                throw new InsufficientStockException("Not enough stock to increase quantity.");
            }

            book.setQuantity(book.getQuantity() - quantityDifference);
            bookRepository.save(book);

            double priceDifference = existing.getUnitPrice().doubleValue() * quantityDifference;
            order.setTotalPrice(order.getTotalPrice() + priceDifference);
            orderRepository.save(order);
        }

        orderItemMapper.patchEntityFromRequest(request, existing);
        return orderItemMapper.toResponse(orderItemRepository.save(existing));
    }

    @Transactional
    public void delete(UUID id) {
        OrderItem existing = getOrThrow(id);
        Order order = existing.getOrder();

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderException("Cannot delete items from an order that is not PENDING.");
        }

        Book book = existing.getBook();
        book.setQuantity(book.getQuantity() + existing.getQuantity());
        bookRepository.save(book);

        double lineTotal = existing.getUnitPrice().doubleValue() * existing.getQuantity();
        order.setTotalPrice(order.getTotalPrice() - lineTotal);
        orderRepository.save(order);

        orderItemRepository.delete(existing);
    }

    private OrderItem getOrThrow(UUID id) {
        return orderItemRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order item not found: " + id));
    }
}