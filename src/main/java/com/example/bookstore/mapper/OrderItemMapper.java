package com.example.bookstore.mapper;

import com.example.bookstore.dto.OrderItemResponse;
import com.example.bookstore.dto.OrderItemRequest;
import com.example.bookstore.dto.OrderItemPatchRequest;
import com.example.bookstore.domain.OrderItem;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(source = "id", target = "orderItemId")
    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "book.id", target = "bookId")
    OrderItemResponse toResponse(OrderItem orderItem);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "book", ignore = true)
    OrderItem toEntity(OrderItemRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "book", ignore = true)
    void updateEntityFromRequest(OrderItemRequest request, @MappingTarget OrderItem orderItem);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "book", ignore = true)
    void patchEntityFromRequest(OrderItemPatchRequest request, @MappingTarget OrderItem orderItem);
}