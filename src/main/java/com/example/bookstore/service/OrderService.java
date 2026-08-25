package com.example.bookstore.service;

import com.example.bookstore.domain.Book;
import com.example.bookstore.domain.Cart;
import com.example.bookstore.domain.CartItem;
import com.example.bookstore.domain.Order;
import com.example.bookstore.domain.OrderItem;
import com.example.bookstore.domain.OrderStatus;
import com.example.bookstore.domain.User;
import com.example.bookstore.dto.OrderPatchRequest;
import com.example.bookstore.dto.OrderRequest;
import com.example.bookstore.dto.OrderResponse;
import com.example.bookstore.exception.BookNotFoundException;
import com.example.bookstore.exception.CartNotFoundException;
import com.example.bookstore.exception.InsufficientStockException;
import com.example.bookstore.exception.InvalidOrderException;
import com.example.bookstore.exception.OrderNotFoundException;
import com.example.bookstore.exception.UserNotFoundException;
import com.example.bookstore.mapper.OrderMapper;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.CartRepository;
import com.example.bookstore.repository.OrderRepository;
import com.example.bookstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final CartRepository cartRepository;
    private final OrderMapper orderMapper;

    private String mapSortProperty(String frontendProperty) {
        if ("orderDate".equals(frontendProperty)) {
            return "createdAt";
        }
        if ("totalAmount".equals(frontendProperty)) {
            return "totalPrice";
        }
        return frontendProperty;
    }

    private Pageable createPageable(Integer page, Integer size, String sort) {
        Sort sortObj = Sort.unsorted();
        if (sort != null && sort.contains(",")) {
            String[] sortParams = sort.split(",");
            String sortBy = mapSortProperty(sortParams[0]);
            sortObj = Sort.by(Sort.Direction.fromString(sortParams[1]), sortBy);
        } else if (sort != null) {
            String sortBy = mapSortProperty(sort);
            sortObj = Sort.by(Sort.Direction.ASC, sortBy);
        }
        return PageRequest.of(page != null ? page : 0, size != null ? size : 20, sortObj);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(UUID customerId, OrderStatus status, Integer page, Integer size, String sort) {
        Pageable pageable = createPageable(page, size, sort);
        return orderRepository.searchOrders(customerId, status, pageable)
                .map(orderMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));
        return orderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByUser(UUID userId, Integer page, Integer size, String sort) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found: " + userId);
        }
        Pageable pageable = createPageable(page, size, sort);

        return orderRepository.findByCustomer_UserId(userId, pageable)
                .map(orderMapper::toResponse);
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        User customer = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + request.getUserId()));

        Order order = orderMapper.toEntity(request);
        order.setCustomer(customer);

        if (order.getCreatedAt() == null) {
            order.setCreatedAt(LocalDateTime.now());
        }
        if (order.getUpdatedAt() == null) {
            order.setUpdatedAt(LocalDateTime.now());
        }

        List<OrderItem> orderItems = new ArrayList<>();
        double totalPrice = 0.0;

        for (var itemRequest : request.getItems()) {
            Book book = bookRepository.findById(itemRequest.getBookId())
                    .orElseThrow(() -> new BookNotFoundException("Book not found: " + itemRequest.getBookId()));

            if (book.getQuantity() < itemRequest.getQuantity()) {
                throw new InsufficientStockException("Not enough stock for book: " + book.getTitle());
            }

            book.setQuantity(book.getQuantity() - itemRequest.getQuantity());
            bookRepository.save(book);

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
        order.setStatus(OrderStatus.PENDING);

        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse checkoutCart(UUID userId) {
        Cart cart = cartRepository.findByCustomer_UserId(userId)
                .orElseThrow(() -> new CartNotFoundException("No cart found for user: " + userId));

        if (cart.getCartItems().isEmpty()) {
            throw new InvalidOrderException("Cannot checkout an empty cart.");
        }

        Order order = new Order();
        order.setCustomer(cart.getCustomer());
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        List<OrderItem> orderItems = new ArrayList<>();
        double totalPrice = 0.0;

        for (CartItem cartItem : cart.getCartItems()) {
            Book book = cartItem.getBook();

            if (book.getQuantity() < cartItem.getQuantity()) {
                throw new InsufficientStockException("Not enough stock for book: " + book.getTitle());
            }

            book.setQuantity(book.getQuantity() - cartItem.getQuantity());
            bookRepository.save(book);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setBook(book);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(book.getPrice());

            totalPrice += (book.getPrice().doubleValue() * cartItem.getQuantity());
            orderItems.add(orderItem);
        }

        order.setOrderItems(orderItems);
        order.setTotalPrice(totalPrice);
        Order savedOrder = orderRepository.save(order);

        cart.getCartItems().clear();
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return orderMapper.toResponse(savedOrder);
    }

    @Transactional
    public OrderResponse update(UUID id, OrderRequest request) {
        Order existing = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));

        orderMapper.updateEntityFromRequest(request, existing);
        existing.setUpdatedAt(LocalDateTime.now());
        return orderMapper.toResponse(orderRepository.save(existing));
    }

    @Transactional
    public OrderResponse patch(UUID id, OrderPatchRequest request) {
        Order existing = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));

        if (request.getStatus() != null) {
            existing.setStatus(OrderStatus.valueOf(request.getStatus().getValue()));
            existing.setUpdatedAt(LocalDateTime.now());
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

    @Scheduled(fixedRate = 10000)
    @Transactional
    public void autoCompletedShippedOrders() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(2);

        List<Order> shippedOrders = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.SHIPPED)
                .filter(o -> o.getUpdatedAt() != null && o.getUpdatedAt().isBefore(threshold))
                .toList();

        for (Order order : shippedOrders) {
            order.setStatus(OrderStatus.COMPLETED);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
        }
    }

    @Transactional
    public OrderResponse payOrder(UUID orderId, org.springframework.security.oauth2.jwt.Jwt jwt) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        UUID currentUserId = UUID.fromString(jwt.getSubject());

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));

        if (!isAdmin && !order.getCustomer().getUserId().equals(currentUserId)) {
            throw new InvalidOrderException("Unauthorized: You can only pay for your own orders.");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderException("Order cannot be paid unless it is pending.");
        }

        order.setStatus(OrderStatus.SHIPPED);
        order.setUpdatedAt(LocalDateTime.now());

        return orderMapper.toResponse(orderRepository.save(order));
    }
    @Transactional
    public void updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }
}