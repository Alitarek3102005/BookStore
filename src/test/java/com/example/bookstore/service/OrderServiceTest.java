package com.example.bookstore.service;

import com.example.bookstore.domain.Book;
import com.example.bookstore.domain.Order;
import com.example.bookstore.domain.OrderItem;
import com.example.bookstore.dto.OrderItemLine;
import com.example.bookstore.domain.OrderStatus;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

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

    @Mock
    private Jwt jwt;

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

    @AfterEach
    void tearDownSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContext(String role) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        if (role != null) {
            doReturn(List.of(new SimpleGrantedAuthority(role)))
                    .when(authentication).getAuthorities();
        } else {
            doReturn(List.of()).when(authentication).getAuthorities();
        }
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
        UUID userId = UUID.randomUUID();
        Order mockOrder = new Order();
        OrderResponse mockResponse = new OrderResponse();

        when(userRepository.existsById(userId)).thenReturn(true);
        when(orderRepository.findByCustomer_UserId(userId)).thenReturn(List.of(mockOrder));
        when(orderMapper.toResponse(mockOrder)).thenReturn(mockResponse);

        List<OrderResponse> result = orderService.getOrdersByUser(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository).findByCustomer_UserId(userId);
    }

    @Test
    void getOrdersByUser_ShouldReturnEmptyList_WhenNoOrdersFound() {
        UUID userId = UUID.randomUUID();

        when(userRepository.existsById(userId)).thenReturn(true);
        when(orderRepository.findByCustomer_UserId(userId)).thenReturn(List.of());

        List<OrderResponse> result = orderService.getOrdersByUser(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty(), "The result should be an empty list");
        verify(orderRepository).findByCustomer_UserId(userId);

        verify(orderMapper, never()).toResponse(any());
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


    @Test
    void payOrder_ShouldSucceed_WhenUserIsOwnerAndOrderIsPending() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));
        when(jwt.getSubject()).thenReturn(userId.toString());
        mockSecurityContext(null);

        when(orderRepository.save(any(Order.class))).thenReturn(orderEntity);
        when(orderMapper.toResponse(orderEntity)).thenReturn(orderResponse);

        OrderResponse result = orderService.payOrder(orderId, jwt);

        assertNotNull(result);
        assertEquals(OrderStatus.SHIPPED, orderEntity.getStatus());
        verify(orderRepository).save(orderEntity);
    }

    @Test
    void payOrder_ShouldSucceed_WhenUserIsAdminAndOrderIsPending() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));

        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        mockSecurityContext("ROLE_ADMIN");

        when(orderRepository.save(any(Order.class))).thenReturn(orderEntity);
        when(orderMapper.toResponse(orderEntity)).thenReturn(orderResponse);

        OrderResponse result = orderService.payOrder(orderId, jwt);

        assertNotNull(result);
        assertEquals(OrderStatus.SHIPPED, orderEntity.getStatus());
        verify(orderRepository).save(orderEntity);
    }

    @Test
    void payOrder_ShouldThrowException_WhenOrderNotFound() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.payOrder(orderId, jwt));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void payOrder_ShouldThrowException_WhenUserIsNotOwnerOrAdmin() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));

        when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
        mockSecurityContext(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> orderService.payOrder(orderId, jwt));
        assertEquals("Unauthorized: You can only pay for your own orders.", exception.getMessage());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void payOrder_ShouldThrowException_WhenOrderIsNotPending() {
        orderEntity.setStatus(OrderStatus.SHIPPED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));
        when(jwt.getSubject()).thenReturn(userId.toString());
        mockSecurityContext(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> orderService.payOrder(orderId, jwt));
        assertEquals("Order cannot be paid unless it is pending.", exception.getMessage());
        verify(orderRepository, never()).save(any());
    }
}