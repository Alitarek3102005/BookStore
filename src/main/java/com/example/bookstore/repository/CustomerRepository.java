package com.example.bookstore.repository;
import com.example.bookstore.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface CustomerRepository extends JpaRepository <Customer,UUID>{}
