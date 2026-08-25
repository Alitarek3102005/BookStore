package com.example.bookstore.service;

import com.example.bookstore.domain.Book;
import com.example.bookstore.domain.Category;
import com.example.bookstore.dto.BookPatchRequest;
import com.example.bookstore.dto.BookRequest;
import com.example.bookstore.dto.BookResponse;
import com.example.bookstore.exception.BookNotFoundException;
import com.example.bookstore.exception.CategoryNotFoundException;
import com.example.bookstore.mapper.BookMapper;
import com.example.bookstore.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final BookMapper bookMapper;
    private final S3StorageService s3StorageService;

    @Transactional(readOnly = true)
    public List<BookResponse> searchBooks(String title, String author, UUID categoryId, Boolean active, Integer page, Integer size, String sort) {
        Sort sortObj = Sort.unsorted();
        if (sort != null && sort.contains(",")) {
            String[] sortParams = sort.split(",");
            sortObj = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
        } else if (sort != null) {
            sortObj = Sort.by(Sort.Direction.ASC, sort);
        }

        Pageable pageable = PageRequest.of(page != null ? page : 0, size != null ? size : 20, sortObj);

        Page<Book> bookPage = bookRepository.searchBooks(title, author, categoryId, active, pageable);
        return bookPage.stream().map(bookMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<BookResponse> getBooksByCategory(UUID categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new CategoryNotFoundException("Category not found: " + categoryId);
        }
        return bookRepository.findByCategory_Id(categoryId).stream()
                .map(bookMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BookResponse getById(UUID id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found: " + id));
        return bookMapper.toResponse(book);
    }

    @Transactional
    public BookResponse addBook(BookRequest bookDto, MultipartFile imageFile) {
        Category category = categoryRepository.findById(bookDto.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + bookDto.getCategoryId()));

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String imageUrl = s3StorageService.uploadFile(imageFile);
                bookDto.setImgURL(URI.create(imageUrl));
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to upload image to S3: " + e.getMessage(), e);
            }
        }

        Book book = bookMapper.toEntity(bookDto);
        book.setCategory(category);

        if (book.getActive() == null) {
            book.setActive(true);
        }

        return bookMapper.toResponse(bookRepository.save(book));
    }

    @Transactional
    public BookResponse updateBook(UUID id, BookRequest bookDto) {
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found: " + id));
        Category category = categoryRepository.findById(bookDto.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + bookDto.getCategoryId()));

        Book updatedBook = bookMapper.toEntity(bookDto);
        updatedBook.setId(existingBook.getId());
        updatedBook.setCategory(category);

        return bookMapper.toResponse(bookRepository.save(updatedBook));
    }

    @Transactional
    public BookResponse patchBook(UUID id, BookPatchRequest bookDto) {
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found: " + id));

        if (bookDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(bookDto.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + bookDto.getCategoryId()));
            existingBook.setCategory(category);
        }

        bookMapper.patchEntityFromRequest(bookDto, existingBook);
        return bookMapper.toResponse(bookRepository.save(existingBook));
    }

    @Transactional
    public void deleteBook(UUID id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found: " + id));

        book.setActive(false);
        bookRepository.save(book);
    }
}