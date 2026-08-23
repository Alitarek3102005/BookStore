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
import com.example.bookstore.exception.CartNotFoundException;
import com.example.bookstore.exception.InsufficientStockException;
import com.example.bookstore.exception.UserNotFoundException;
import com.example.bookstore.mapper.CartMapper;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.CartItemRepository;
import com.example.bookstore.repository.CartRepository;
import com.example.bookstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final CartMapper cartMapper;


    @Transactional(readOnly = true)
    public List<CartResponse> getAllCarts() {
        return cartRepository.findAll().stream()
                .map(cartMapper::toResponse)
                .toList();
    }

    @Transactional
    public CartResponse getOrCreateCart(UUID userId) {
        return cartMapper.toResponse(findOrCreateCartEntity(userId));
    }

    @Transactional
    public CartResponse addItem(UUID userId, CartItemRequest request) {
        Cart cart = findOrCreateCartEntity(userId);

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new BookNotFoundException("Book not found: " + request.getBookId()));

        Optional<CartItem> existingItem = cartItemRepository.findByCart_IdAndBook_Id(cart.getId(), book.getId());

        int requestedTotalQuantity = request.getQuantity() + existingItem.map(CartItem::getQuantity).orElse(0);

        if (book.getQuantity() < requestedTotalQuantity) {
            throw new InsufficientStockException("Not enough stock for book: " + book.getTitle());
        }

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
        } else {
            CartItem item = new CartItem();
            item.setBook(book);
            item.setQuantity(request.getQuantity());

            cart.addItem(item);
        }

        cart.setUpdatedAt(LocalDateTime.now());

        Cart savedCart = cartRepository.save(cart);

        return cartMapper.toResponse(savedCart);
    }

    @Transactional
    public CartResponse updateItemQuantity(UUID cartItemId, CartItemPatchRequest request) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found: " + cartItemId));

        if (request.getQuantity() != null) {
            if (item.getBook().getQuantity() < request.getQuantity()) {
                throw new InsufficientStockException("Not enough stock for book: " + item.getBook().getTitle());
            }
            item.setQuantity(request.getQuantity());
        }

        Cart cart = item.getCart();
        cart.setUpdatedAt(LocalDateTime.now());

        cartRepository.save(cart);

        return cartMapper.toResponse(cart);
    }

    @Transactional
    public void removeItem(UUID cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found: " + cartItemId));

        Cart cart = item.getCart();

        cart.removeItem(item);
        cart.setUpdatedAt(LocalDateTime.now());

        cartRepository.save(cart);
    }


    @Transactional
    public void clearCart(UUID userId) {
        Cart cart = cartRepository.findByCustomer_UserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));

        cart.getCartItems().clear();
        cart.setUpdatedAt(LocalDateTime.now());

        cartRepository.save(cart);
    }

    private Cart findOrCreateCartEntity(UUID userId) {
        return cartRepository.findByCustomer_UserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

                    Cart cart = new Cart();
                    cart.setCustomer(user);
                    return cartRepository.save(cart);
                });
    }
}