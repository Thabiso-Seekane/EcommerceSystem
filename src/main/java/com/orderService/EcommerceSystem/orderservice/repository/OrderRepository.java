package com.orderService.EcommerceSystem.orderservice.repository;

import com.orderService.EcommerceSystem.orderservice.entity.Order;
import com.orderService.EcommerceSystem.orderservice.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatus(OrderStatus status);
}
