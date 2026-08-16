package com.example.bookstore.service;
import com .example.bookstore.entity.Customer;
import com .example.bookstore.repository.CustomerRepository;
import org.springframework.stereotype.Service;
@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
}
}

