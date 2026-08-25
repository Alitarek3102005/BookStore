package com.example.bookstore.mapper;

import com.example.bookstore.domain.Review;
import com.example.bookstore.dto.ReviewResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(source = "id", target = "reviewId")
    @Mapping(source = "book.id", target = "bookId")
    @Mapping(source = "customer.userId", target = "userId")
    @Mapping(source = "customer.username", target = "username")
    @Mapping(source = "createdAt", target = "createdAt")

    @Mapping(source = "updatedAt", target = "updatedAt")
    ReviewResponse toResponse(Review review);

    default OffsetDateTime map(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }
}