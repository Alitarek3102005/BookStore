package com.example.bookstore.mapper;

import com.example.bookstore.domain.Order;
import com.example.bookstore.dto.OrderRequest;
import com.example.bookstore.dto.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Maps between the Order entity and the OrderRequest / OrderResponse contracts.
 *
 * NOTE: the Order entity currently has no status field and no items relation,
 * both of which the contract requires on OrderResponse. Those are left ignored
 * below rather than guessed at - see TODOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customerId", source = "userId")
    @Mapping(target = "totalprice", ignore = true) // computed server-side from order items, not client-supplied
    @Mapping(target = "createdAt", ignore = true)  // set by @PrePersist
    @Mapping(target = "updatedAt", ignore = true)  // set by @PrePersist/@PreUpdate
    Order toEntity(OrderRequest request);

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "userId", source = "customerId")
    @Mapping(target = "totalAmount", source = "totalprice")
    @Mapping(target = "orderDate", source = "createdAt")
    // TODO: add a status (OrderStatus) field to the Order entity, then map it here
    @Mapping(target = "status", ignore = true)
    // TODO: add a @OneToMany List<OrderItem> to the Order entity + an OrderItemMapper, then map it here
    @Mapping(target = "items", ignore = true)
    OrderResponse toResponse(Order order);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customerId", source = "userId")
    @Mapping(target = "totalprice", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(OrderRequest request, @MappingTarget Order order);

    // MapStruct needs this explicit conversion: entity stores LocalDateTime (no zone info),
    // but the generated OrderResponse.orderDate is OffsetDateTime. Assuming server-local
    // timestamps are UTC here - adjust the offset if your app uses a different convention.
    default OffsetDateTime map(LocalDateTime value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC);
    }
}