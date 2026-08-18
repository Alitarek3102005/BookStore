package com.example.bookstore.mapper;

import com.example.bookstore.domain.Book;
import com.example.bookstore.dto.BookRequest;
import com.example.bookstore.dto.BookResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookMapper {

    @Mapping(source = "id", target = "bookId")
    BookResponse toResponse(Book book);

    @Mapping(target = "id", ignore = true)
    Book toEntity(BookRequest request);
}