package com.example.bookstore.service;

import com.example.bookstore.domain.Book;
import com.example.bookstore.domain.Order;
import com.example.bookstore.domain.OrderItem;
import com.example.bookstore.dto.OrderItemLine;
import com.example.bookstore.domain.OrderStatus;
import com.example.bookstore.domain.User;
import com.example.bookstore.dto.OrderItemRequest;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    private UUID orderId;
    private UUID userId;
    private UUID bookId;

    private Order orderEntity;
    private User userEntity;
    private Book bookEntity;
    private OrderRequest orderRequest;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        userId = UUID.randomUUID();
        bookId = UUID.randomUUID();

        userEntity = new User();
        userEntity.setUserId(userId);
        userEntity.setUsername("testcustomer");

        bookEntity = new Book();
        bookEntity.setId(bookId);
        bookEntity.setPrice(BigDecimal.valueOf(25.50));
        bookEntity.setQuantity(10);

        orderEntity = new Order();
        orderEntity.setId(orderId);
        orderEntity.setCustomer(userEntity);
        orderEntity.setStatus(OrderStatus.PENDING);

        orderRequest = new OrderRequest();
        orderRequest.setUserId(userId);

        OrderItemLine itemRequest = new OrderItemLine();
        itemRequest.setBookId(bookId);
        itemRequest.setQuantity(2);

        orderRequest.setItems(Collections.singletonList(itemRequest));

        orderResponse = new OrderResponse();
    }


    @Test
    void getAllOrders_ShouldReturnList() {
        when(orderRepository.findAll()).thenReturn(List.of(orderEntity));
        when(orderMapper.toResponse(orderEntity)).thenReturn(orderResponse);

        List<OrderResponse> result = orderService.getAllOrders();

        assertEquals(1, result.size());
        verify(orderRepository).findAll();
    }

    @Test
    void getById_ShouldReturnOrder_WhenExists() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));
        when(orderMapper.toResponse(orderEntity)).thenReturn(orderResponse);

        OrderResponse result = orderService.getById(orderId);

        assertNotNull(result);
    }

    @Test
    void getById_ShouldThrowException_WhenOrderNotFound() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.getById(orderId));
    }

    @Test
    void getOrdersByUser_ShouldReturnList_WhenUserExists() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(orderRepository.findByCustomer_UserId(userId)).thenReturn(List.of(orderEntity));
        when(orderMapper.toResponse(orderEntity)).thenReturn(orderResponse);

        List<OrderResponse> result = orderService.getOrdersByUser(userId);

        assertEquals(1, result.size());
        verify(orderRepository).findByCustomer_UserId(userId);
    }

    @Test
    void getOrdersByUser_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> orderService.getOrdersByUser(userId));
        verify(orderRepository, never()).findByCustomer_UserId(any());
    }

    @Test
    void createOrder_ShouldCalculateTotalPriceAndSave() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(orderMapper.toEntity(orderRequest)).thenReturn(new Order());
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(bookEntity));

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponse);

        OrderResponse result = orderService.createOrder(orderRequest);

        assertNotNull(result);

        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();

        assertEquals(51.0, savedOrder.getTotalPrice());
        verify(bookRepository).findById(bookId);
    }

    @Test
    void createOrder_ShouldThrowException_WhenBookNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(orderMapper.toEntity(orderRequest)).thenReturn(new Order());
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> orderService.createOrder(orderRequest));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> orderService.createOrder(orderRequest));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void update_ShouldUpdateAndSave_WhenOrderExists() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));
        when(orderRepository.save(orderEntity)).thenReturn(orderEntity);
        when(orderMapper.toResponse(orderEntity)).thenReturn(orderResponse);

        OrderResponse result = orderService.update(orderId, orderRequest);

        assertNotNull(result);
        verify(orderMapper).updateEntityFromRequest(orderRequest, orderEntity);
        verify(orderRepository).save(orderEntity);
    }

    @Test
    void patch_ShouldUpdateStatusAndSave_WhenOrderExists() {
        OrderPatchRequest patchRequest = new OrderPatchRequest();
        OrderPatchRequest.StatusEnum statusEnum = OrderPatchRequest.StatusEnum.SHIPPED;
        patchRequest.setStatus(statusEnum);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));
        when(orderRepository.save(orderEntity)).thenReturn(orderEntity);
        when(orderMapper.toResponse(orderEntity)).thenReturn(orderResponse);

        OrderResponse result = orderService.patch(orderId, patchRequest);

        assertNotNull(result);
        assertEquals(OrderStatus.SHIPPED, orderEntity.getStatus());
        verify(orderRepository).save(orderEntity);
    }

    @Test
    void delete_ShouldDelete_WhenOrderExists() {
        when(orderRepository.existsById(orderId)).thenReturn(true);

        orderService.delete(orderId);

        verify(orderRepository).deleteById(orderId);
    }

    @Test
    void delete_ShouldThrowException_WhenOrderNotFound() {
        when(orderRepository.existsById(orderId)).thenReturn(false);

        assertThrows(OrderNotFoundException.class, () -> orderService.delete(orderId));
        verify(orderRepository, never()).deleteById(any());
    }
}