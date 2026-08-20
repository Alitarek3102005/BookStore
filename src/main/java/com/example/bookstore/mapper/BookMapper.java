package com.example.bookstore.mapper;

import com.example.bookstore.domain.Book;
import com.example.bookstore.dto.BookPatchRequest;
import com.example.bookstore.dto.BookRequest;
import com.example.bookstore.dto.BookResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.net.URI;

@Mapper(componentModel = "spring")
public interface BookMapper {

    @Mapping(source = "id", target = "bookId")
    @Mapping(source = "category.id", target = "categoryId")
    BookResponse toResponse(Book book);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    Book toEntity(BookRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    void patchEntityFromRequest(BookPatchRequest request, @MappingTarget Book book);
    default URI mapStringToUri(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return URI.create(value);
    }
    default String mapUriToString(URI value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }
}