package com.example.bookstore.mapper;

import com.example.bookstore.dto.OrderItemResponse;
import com.example.bookstore.dto.OrderItemRequest;
import com.example.bookstore.dto.OrderItemPatchRequest;

import com.example.bookstore.domain.OrderItem;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(source = "id", target = "orderItemId")
    OrderItemResponse toResponse(OrderItem orderItem);


    @Mapping(target = "id", ignore = true)
    OrderItem toEntity(OrderItemRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "bookId", ignore = true)
    void updateEntityFromRequest(OrderItemRequest request, @MappingTarget OrderItem orderItem);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "bookId", ignore = true)
    void patchEntityFromRequest(OrderItemPatchRequest request, @MappingTarget OrderItem orderItem);
}
