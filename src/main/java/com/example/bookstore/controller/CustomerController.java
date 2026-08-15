package com.example.bookstore.controller;
import com.example.bookstore.entity.Customer;
import com .example.bookstore.service.CustomerService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
@RestController
@RequestMapping ("/customers")
public class CustomerController {
   private final CustomerService customerService;
   public CustomerController(CustomerService customerService) {
       this.customerService = customerService;
   }
@PostMapping
    public Customer createCustomer (@RequestBody Customer customer) {
       return customerService.saveCustomer(customer);

}

}
