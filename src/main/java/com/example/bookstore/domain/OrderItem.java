package com.example.bookstore.domain;


import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name= "OrderId", nullable = false)
    @NotNull
    private UUID orderId;

    @Column(name = "BookId",nullable = false)
    @NotNull
    private UUID bookId;

    @Column(name = "Quantity", nullable = false)
    @Min(1)
    @NotNull
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    @NotNull
    private BigDecimal unitPrice;

}