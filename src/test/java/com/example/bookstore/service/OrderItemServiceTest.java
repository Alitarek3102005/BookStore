package com.example.bookstore.service;

import com.example.bookstore.domain.Book;
import com.example.bookstore.domain.Order;
import com.example.bookstore.domain.OrderItem;
import com.example.bookstore.dto.OrderItemPatchRequest;
import com.example.bookstore.dto.OrderItemRequest;
import com.example.bookstore.dto.OrderItemResponse;
import com.example.bookstore.exception.BookNotFoundException;
import com.example.bookstore.exception.DuplicateResourceException;
import com.example.bookstore.exception.InvalidOrderException;
import com.example.bookstore.exception.OrderNotFoundException;
import com.example.bookstore.mapper.OrderItemMapper;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.OrderItemRepository;
import com.example.bookstore.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderItemServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private OrderItemMapper orderItemMapper;

    @InjectMocks
    private OrderItemService orderItemService;

    private UUID orderItemId;
    private UUID orderId;
    private UUID bookId;

    private Order orderEntity;
    private Book bookEntity;
    private OrderItem orderItemEntity;
    private OrderItemRequest request;
    private OrderItemResponse response;

    @BeforeEach
    void setUp() {
        orderItemId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        bookId = UUID.randomUUID();

        orderEntity = new Order();
        orderEntity.setId(orderId);

        bookEntity = new Book();
        bookEntity.setId(bookId);
        bookEntity.setQuantity(10);
        bookEntity.setPrice(BigDecimal.valueOf(15.0));

        orderItemEntity = new OrderItem();
        orderItemEntity.setId(orderItemId);
        orderItemEntity.setOrder(orderEntity);
        orderItemEntity.setBook(bookEntity);
        orderItemEntity.setQuantity(2);
        orderItemEntity.setUnitPrice(BigDecimal.valueOf(15.0));

        request = new OrderItemRequest();
        request.setOrderId(orderId);
        request.setBookId(bookId);
        request.setQuantity(2);

        response = new OrderItemResponse();
        response.setBookId(orderItemId);
    }

    @Test
    void findAll_ShouldReturnList() {
        when(orderItemRepository.findAll()).thenReturn(List.of(orderItemEntity));
        when(orderItemMapper.toResponse(orderItemEntity)).thenReturn(response);

        List<OrderItemResponse> result = orderItemService.findAll();

        assertEquals(1, result.size());
        verify(orderItemRepository).findAll();
    }

    @Test
    void findById_ShouldReturnItem_WhenExists() {
        when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.of(orderItemEntity));
        when(orderItemMapper.toResponse(orderItemEntity)).thenReturn(response);

        OrderItemResponse result = orderItemService.findById(orderItemId);

        assertNotNull(result);
        verify(orderItemRepository).findById(orderItemId);
    }

    @Test
    void findById_ShouldThrowException_WhenNotFound() {
        when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderItemService.findById(orderItemId));
    }

    @Test
    void findByOrderId_ShouldReturnList() {
        when(orderItemRepository.findByOrder_Id(orderId)).thenReturn(List.of(orderItemEntity));
        when(orderItemMapper.toResponse(orderItemEntity)).thenReturn(response);

        List<OrderItemResponse> result = orderItemService.findByOrderId(orderId);

        assertEquals(1, result.size());
        verify(orderItemRepository).findByOrder_Id(orderId);
    }

    @Test
    void delete_ShouldDeleteItem_WhenExists() {
        when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.of(orderItemEntity));

        orderItemService.delete(orderItemId);

        verify(orderItemRepository).delete(orderItemEntity);
    }

    @Test
    void delete_ShouldThrowException_WhenNotFound() {
        when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderItemService.delete(orderItemId));
        verify(orderItemRepository, never()).delete(any());
    }

    @Test
    void save_ShouldSaveItem_WhenValidAndUnique() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(bookEntity));
        when(orderItemRepository.existsByOrder_IdAndBook_Id(orderId, bookId)).thenReturn(false);

        when(orderItemMapper.toEntity(request)).thenReturn(orderItemEntity);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(orderItemEntity);
        when(orderItemMapper.toResponse(orderItemEntity)).thenReturn(response);

        OrderItemResponse result = orderItemService.save(request);

        assertNotNull(result);
        verify(orderItemRepository).save(any(OrderItem.class));
    }

    @Test
    void save_ShouldThrowException_WhenOrderNotFound() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderItemService.save(request));
        verify(orderItemRepository, never()).save(any());
    }

    @Test
    void save_ShouldThrowException_WhenBookNotFound() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> orderItemService.save(request));
        verify(orderItemRepository, never()).save(any());
    }

    @Test
    void save_ShouldThrowException_WhenDuplicateBookInOrder() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(bookEntity));
        when(orderItemRepository.existsByOrder_IdAndBook_Id(orderId, bookId)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> orderItemService.save(request));
        verify(orderItemRepository, never()).save(any());
    }

    @Test
    void update_ShouldUpdateAndSave_WhenIdsMatch() {
        when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.of(orderItemEntity));
        when(orderItemRepository.save(orderItemEntity)).thenReturn(orderItemEntity);
        when(orderItemMapper.toResponse(orderItemEntity)).thenReturn(response);

        OrderItemResponse result = orderItemService.update(orderItemId, request);

        assertNotNull(result);
        verify(orderItemMapper).updateEntityFromRequest(request, orderItemEntity);
        verify(orderItemRepository).save(orderItemEntity);
    }

    @Test
    void update_ShouldThrowException_WhenOrderIdIsChanged() {
        when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.of(orderItemEntity));
        request.setOrderId(UUID.randomUUID());

        assertThrows(InvalidOrderException.class, () -> orderItemService.update(orderItemId, request));
        verify(orderItemRepository, never()).save(any());
    }

    @Test
    void update_ShouldThrowException_WhenBookIdIsChanged() {
        when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.of(orderItemEntity));

        request.setBookId(UUID.randomUUID());

        assertThrows(InvalidOrderException.class, () -> orderItemService.update(orderItemId, request));
        verify(orderItemRepository, never()).save(any());
    }

    @Test
    void patch_ShouldUpdateAndSave_WhenExists() {
        OrderItemPatchRequest patchRequest = new OrderItemPatchRequest();
        patchRequest.setQuantity(5);

        when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.of(orderItemEntity));
        when(orderItemRepository.save(orderItemEntity)).thenReturn(orderItemEntity);
        when(orderItemMapper.toResponse(orderItemEntity)).thenReturn(response);

        OrderItemResponse result = orderItemService.patch(orderItemId, patchRequest);

        assertNotNull(result);
        verify(orderItemMapper).patchEntityFromRequest(patchRequest, orderItemEntity);
        verify(orderItemRepository).save(orderItemEntity);
    }
}