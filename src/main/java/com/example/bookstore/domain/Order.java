package com.example.bookstore.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.engine.internal.Nullability;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @NotNull
    @Column(name ="userId",nullable = false)
    private UUID customerId;
    @NotNull
    private double totalprice;
    @Column(name = "createdAt", nullable = false,updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist(){
        LocalDateTime now=LocalDateTime.now();
        createdAt = now;
        updatedAt = now;

    }
    @PreUpdate
    public void preUpdate(){updatedAt=LocalDateTime.now();

    }

}