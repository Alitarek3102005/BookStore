package com.example.bookstore.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class User {

    @Id
    private UUID userId;

    private String username;
    private String email;
    private String address;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String password;
    private boolean enabled;
}