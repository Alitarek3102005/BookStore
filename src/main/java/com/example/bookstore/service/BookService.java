package com.example.bookstore.service;

import com.example.bookstore.domain.Book;
import com.example.bookstore.dto.BookRequest;
import com.example.bookstore.dto.BookResponse;
import com.example.bookstore.mapper.BookMapper;
import com.example.bookstore.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public BookResponse addBook(BookRequest bookDto) {
        Book book = bookMapper.toEntity(bookDto);
        return bookMapper.toResponse(bookRepository.save(book));

    }
    public BookResponse updateBook(UUID id ,BookRequest bookDto){
        Book book = bookRepository.findById(id).orElseThrow();
        Book entity = bookMapper.toEntity(bookDto);
        book=entity;
        return bookMapper.toResponse(bookRepository.save(book));

    }
    public void deleteBook(UUID id){
        bookRepository.deleteById(id);
    }
    public List<com.example.bookstore.dto.BookResponse> getAll(){
        List<Book> books=bookRepository.findAll();
        List<com.example.bookstore.dto.BookResponse> bookDtos =new ArrayList<>();
        for(Book book:books){
            bookDtos.add(bookMapper.toResponse(book));
        }
        return  bookDtos;
    }
    public BookResponse getById(UUID id){
        return bookMapper.toResponse( bookRepository.findById(id).orElseThrow(()-> new RuntimeException()));
    }


}