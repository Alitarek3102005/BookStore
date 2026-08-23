package com.example.bookstore.service;

import com.example.bookstore.domain.Book;
import com.example.bookstore.domain.Category;
import com.example.bookstore.dto.BookPatchRequest;
import com.example.bookstore.dto.BookRequest;
import com.example.bookstore.dto.BookResponse;
import com.example.bookstore.exception.BookNotFoundException;
import com.example.bookstore.exception.CategoryNotFoundException;
import com.example.bookstore.mapper.BookMapper;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.CategoryRepository;
import com.example.bookstore.repository.OrderItemRepository;
import com.example.bookstore.repository.CartItemRepository;
import com.example.bookstore.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookService bookService;

    private UUID bookId;
    private UUID categoryId;
    private Book bookEntity;
    private Category categoryEntity;
    private BookRequest bookRequest;
    private BookResponse bookResponse;

    @BeforeEach
    void setUp() {
        bookId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        categoryEntity = new Category();
        categoryEntity.setId(categoryId);
        categoryEntity.setName("Science Fiction");

        bookEntity = new Book();
        bookEntity.setId(bookId);
        bookEntity.setTitle("Dune");
        bookEntity.setCategory(categoryEntity);

        bookRequest = new BookRequest();
        bookRequest.setTitle("Dune");
        bookRequest.setCategoryId(categoryId);

        bookResponse = new BookResponse();
        bookResponse.setBookId(bookId);
        bookResponse.setTitle("Dune");
    }

    @Test
    void searchBooks_ShouldReturnListOfBooks_WithPaginationAndSorting() {
        Page<Book> pagedResponse = new PageImpl<>(List.of(bookEntity));
        when(bookRepository.searchBooks(any(), any(), any(), any(Pageable.class))).thenReturn(pagedResponse);
        when(bookMapper.toResponse(bookEntity)).thenReturn(bookResponse);

        List<BookResponse> result = bookService.searchBooks("Dune", null, categoryId, 0, 10, "title,ASC");
        assertEquals(1, result.size());
        assertEquals("Dune", result.get(0).getTitle());
        verify(bookRepository).searchBooks(eq("Dune"), isNull(), eq(categoryId), any(Pageable.class));
    }

    @Test
    void getBooksByCategory_ShouldReturnList_WhenCategoryExists() {
        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        when(bookRepository.findByCategory_Id(categoryId)).thenReturn(List.of(bookEntity));
        when(bookMapper.toResponse(bookEntity)).thenReturn(bookResponse);

        List<BookResponse> result = bookService.getBooksByCategory(categoryId);

        assertEquals(1, result.size());
        verify(bookRepository).findByCategory_Id(categoryId);
    }

    @Test
    void getBooksByCategory_ShouldThrowException_WhenCategoryDoesNotExist() {
        when(categoryRepository.existsById(categoryId)).thenReturn(false);

        assertThrows(CategoryNotFoundException.class, () -> bookService.getBooksByCategory(categoryId));
        verify(bookRepository, never()).findByCategory_Id(any());
    }

    @Test
    void getById_ShouldReturnBook_WhenExists() {
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(bookEntity));
        when(bookMapper.toResponse(bookEntity)).thenReturn(bookResponse);

        BookResponse result = bookService.getById(bookId);

        assertNotNull(result);
        assertEquals(bookId, result.getBookId());
    }

    @Test
    void getById_ShouldThrowException_WhenBookDoesNotExist() {
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> bookService.getById(bookId));
    }

    @Test
    void addBook_ShouldSaveAndReturnBook_WhenCategoryExists() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(categoryEntity));
        when(bookMapper.toEntity(bookRequest)).thenReturn(bookEntity);
        when(bookRepository.save(any(Book.class))).thenReturn(bookEntity);
        when(bookMapper.toResponse(bookEntity)).thenReturn(bookResponse);

        BookResponse result = bookService.addBook(bookRequest);

        assertNotNull(result);
        verify(categoryRepository).findById(categoryId);
        verify(bookRepository).save(bookEntity);
    }

    @Test
    void addBook_ShouldThrowException_WhenCategoryDoesNotExist() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> bookService.addBook(bookRequest));
        verify(bookRepository, never()).save(any());
    }

    @Test
    void updateBook_ShouldUpdateAndSave_WhenBookAndCategoryExist() {
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(bookEntity));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(categoryEntity));
        when(bookMapper.toEntity(bookRequest)).thenReturn(bookEntity);
        when(bookRepository.save(any(Book.class))).thenReturn(bookEntity);
        when(bookMapper.toResponse(bookEntity)).thenReturn(bookResponse);

        BookResponse result = bookService.updateBook(bookId, bookRequest);

        assertNotNull(result);
        assertEquals(bookId, bookEntity.getId());
        assertEquals(categoryEntity, bookEntity.getCategory());
        verify(bookRepository).save(bookEntity);
    }

    @Test
    void updateBook_ShouldThrowException_WhenBookDoesNotExist() {
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> bookService.updateBook(bookId, bookRequest));
        verify(categoryRepository, never()).findById(any());
    }

    @Test
    void patchBook_ShouldUpdateCategory_WhenCategoryIdIsProvided() {
        BookPatchRequest patchRequest = new BookPatchRequest();
        patchRequest.setCategoryId(categoryId);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(bookEntity));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(categoryEntity));
        when(bookRepository.save(bookEntity)).thenReturn(bookEntity);
        when(bookMapper.toResponse(bookEntity)).thenReturn(bookResponse);

        BookResponse result = bookService.patchBook(bookId, patchRequest);

        assertNotNull(result);
        verify(categoryRepository).findById(categoryId);
        verify(bookMapper).patchEntityFromRequest(patchRequest, bookEntity);
        verify(bookRepository).save(bookEntity);
    }

    @Test
    void patchBook_ShouldSkipCategoryUpdate_WhenCategoryIdIsNull() {
        BookPatchRequest patchRequest = new BookPatchRequest();
        patchRequest.setCategoryId(null);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(bookEntity));
        when(bookRepository.save(bookEntity)).thenReturn(bookEntity);
        when(bookMapper.toResponse(bookEntity)).thenReturn(bookResponse);

        BookResponse result = bookService.patchBook(bookId, patchRequest);

        assertNotNull(result);
        verify(categoryRepository, never()).findById(any());
        verify(bookMapper).patchEntityFromRequest(patchRequest, bookEntity);
    }

    @Test
    void deleteBook_ShouldDelete_WhenBookExists() {
        when(bookRepository.existsById(bookId)).thenReturn(true);
        when(orderItemRepository.existsByBook_Id(bookId)).thenReturn(false);

        // Mock the cart and review deletion calls executed during deleteBook
        doNothing().when(cartItemRepository).deleteByBook_Id(bookId);
        doNothing().when(reviewRepository).deleteByBook_Id(bookId);

        bookService.deleteBook(bookId);

        verify(bookRepository).deleteById(bookId);
    }

    @Test
    void deleteBook_ShouldThrowException_WhenBookDoesNotExist() {
        when(bookRepository.existsById(bookId)).thenReturn(false);

        assertThrows(BookNotFoundException.class, () -> bookService.deleteBook(bookId));
        verify(bookRepository, never()).deleteById(any());
    }
}