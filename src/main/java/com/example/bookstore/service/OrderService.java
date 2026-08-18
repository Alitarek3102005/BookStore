package com.example.bookstore.service;

import com.example.bookstore.domain.Order;
import com.example.bookstore.dto.OrderRequest;
import com.example.bookstore.dto.OrderResponse;
import com.example.bookstore.mapper.OrderMapper;
import com.example.bookstore.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    public List<OrderResponse> getAllOrders(){
        List<Order> orders=orderRepository.findAll();
        List<OrderResponse> orderResponses=new ArrayList<>();
        for (Order order:orders){
            orderResponses.add(orderMapper.toResponse(order));

        }
        return orderResponses;

    }
    public OrderResponse getById(UUID Id){
        return orderMapper.toResponse(orderRepository.findById(Id).orElse(null));

    }
    public OrderResponse Create(OrderRequest orderRequest){
        Order order=orderMapper.toEntity(orderRequest);
        orderRepository.save(order);
        return orderMapper.toResponse(order);

    }
    public OrderResponse Update(UUID Id,OrderRequest orderRequest){
        Order order=orderRepository.findById(Id).orElse(null);
        order=orderMapper.toEntity(orderRequest);
        orderRepository.save(order);
        return orderMapper.toResponse(order);

    }
    public  void Delete(UUID Id){
        orderRepository.deleteById(Id);
    }


}
