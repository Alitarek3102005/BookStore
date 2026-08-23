package com.example.bookstore.service;

import com.example.bookstore.domain.Book;
import com.example.bookstore.domain.Cart;
import com.example.bookstore.domain.CartItem;
import com.example.bookstore.domain.User;
import com.example.bookstore.dto.CartItemPatchRequest;
import com.example.bookstore.dto.CartItemRequest;
import com.example.bookstore.dto.CartResponse;
import com.example.bookstore.exception.BookNotFoundException;
import com.example.bookstore.exception.CartItemNotFoundException;
import com.example.bookstore.exception.InsufficientStockException;
import com.example.bookstore.exception.UserNotFoundException;
import com.example.bookstore.mapper.CartMapper;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.CartItemRepository;
import com.example.bookstore.repository.CartRepository;
import com.example.bookstore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartService cartService;

    private UUID userId;
    private UUID bookId;
    private UUID cartItemId;
    private Cart cartEntity;
    private CartItem cartItemEntity;
    private Book bookEntity;
    private CartResponse cartResponse;
    private CartItemRequest cartItemRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        bookId = UUID.randomUUID();
        cartItemId = UUID.randomUUID();

        User userEntity = new User();
        userEntity.setUserId(userId);

        bookEntity = new Book();
        bookEntity.setId(bookId);
        bookEntity.setTitle("Java Programming");
        bookEntity.setPrice(BigDecimal.valueOf(20.0));
        bookEntity.setQuantity(10);

        cartEntity = new Cart();
        cartEntity.setId(UUID.randomUUID());
        cartEntity.setCustomer(userEntity);
        cartEntity.setCartItems(new ArrayList<>());

        cartItemEntity = new CartItem();
        cartItemEntity.setId(cartItemId);
        cartItemEntity.setCart(cartEntity);
        cartItemEntity.setBook(bookEntity);
        cartItemEntity.setQuantity(2);

        cartResponse = new CartResponse();
        cartResponse.setUserId(userId);

        cartItemRequest = new CartItemRequest();
        cartItemRequest.setBookId(bookId);
        cartItemRequest.setQuantity(2);
    }

    @Test
    void getAllCarts_ShouldReturnList() {
        when(cartRepository.findAll()).thenReturn(List.of(cartEntity));
        when(cartMapper.toResponse(cartEntity)).thenReturn(cartResponse);

        List<CartResponse> result = cartService.getAllCarts();

        assertEquals(1, result.size());
        verify(cartRepository).findAll();
    }

    @Test
    void getOrCreateCart_ShouldReturnCart_WhenUserExists() {
        when(cartRepository.findByCustomer_UserId(userId)).thenReturn(Optional.of(cartEntity));
        when(cartMapper.toResponse(cartEntity)).thenReturn(cartResponse);

        CartResponse result = cartService.getOrCreateCart(userId);

        assertNotNull(result);
        verify(cartRepository).findByCustomer_UserId(userId);
    }

    @Test
    void addItem_ShouldAddSuccessfully_WhenStockIsSufficient() {
        when(cartRepository.findByCustomer_UserId(userId)).thenReturn(Optional.of(cartEntity));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(bookEntity));
        when(cartItemRepository.findByCart_IdAndBook_Id(cartEntity.getId(), bookId)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(cartEntity);
        when(cartMapper.toResponse(cartEntity)).thenReturn(cartResponse);

        CartResponse result = cartService.addItem(userId, cartItemRequest);

        assertNotNull(result);
        verify(cartRepository).save(cartEntity);
    }

    @Test
    void addItem_ShouldThrowException_WhenStockIsInsufficient() {
        bookEntity.setQuantity(1); // Only 1 in stock, but request is for 2
        when(cartRepository.findByCustomer_UserId(userId)).thenReturn(Optional.of(cartEntity));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(bookEntity));
        when(cartItemRepository.findByCart_IdAndBook_Id(cartEntity.getId(), bookId)).thenReturn(Optional.empty());

        assertThrows(InsufficientStockException.class, () -> cartService.addItem(userId, cartItemRequest));
        verify(cartRepository, never()).save(any());
    }

    @Test
    void updateItemQuantity_ShouldUpdate_WhenItemExists() {
        CartItemPatchRequest patchRequest = new CartItemPatchRequest();
        patchRequest.setQuantity(4);

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItemEntity));
        when(cartRepository.save(cartEntity)).thenReturn(cartEntity);
        when(cartMapper.toResponse(cartEntity)).thenReturn(cartResponse);

        CartResponse result = cartService.updateItemQuantity(cartItemId, patchRequest);

        assertNotNull(result);
        assertEquals(4, cartItemEntity.getQuantity());
        verify(cartRepository).save(cartEntity);
    }

    @Test
    void updateItemQuantity_ShouldThrowException_WhenItemNotFound() {
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.empty());

        assertThrows(CartItemNotFoundException.class, () -> cartService.updateItemQuantity(cartItemId, new CartItemPatchRequest()));
        verify(cartRepository, never()).save(any());
    }

    @Test
    void removeItem_ShouldRemoveItemFromCart() {
        cartEntity.getCartItems().add(cartItemEntity);

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItemEntity));
        when(cartRepository.save(cartEntity)).thenReturn(cartEntity);

        cartService.removeItem(cartItemId);

        verify(cartItemRepository).findById(cartItemId);
        verify(cartRepository).save(cartEntity);
    }

    @Test
    void clearCart_ShouldClearAllItems() {
        cartEntity.getCartItems().add(cartItemEntity);

        when(cartRepository.findByCustomer_UserId(userId)).thenReturn(Optional.of(cartEntity));
        when(cartRepository.save(cartEntity)).thenReturn(cartEntity);

        cartService.clearCart(userId);

        assertTrue(cartEntity.getCartItems().isEmpty());
        verify(cartRepository).save(cartEntity);
    }
}