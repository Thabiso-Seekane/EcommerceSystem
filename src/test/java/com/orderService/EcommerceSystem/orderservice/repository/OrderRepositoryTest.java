package com.orderService.EcommerceSystem.orderservice.repository;

import com.orderService.EcommerceSystem.orderservice.entity.Order;
import com.orderService.EcommerceSystem.orderservice.entity.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void findByStatus_returnsOrdersWithMatchingStatus() {
        orderRepository.save(Order.builder()
                .status(OrderStatus.NEW)
                .totalAmount(new BigDecimal("100.00"))
                .createdAt(LocalDateTime.now())
                .build());

        orderRepository.save(Order.builder()
                .status(OrderStatus.CANCELLED)
                .totalAmount(new BigDecimal("50.00"))
                .createdAt(LocalDateTime.now())
                .build());

        List<Order> newOrders = orderRepository.findByStatus(OrderStatus.NEW);
        assertThat(newOrders).isNotEmpty();
        assertThat(newOrders).allMatch(o -> o.getStatus() == OrderStatus.NEW);

        List<Order> cancelled = orderRepository.findByStatus(OrderStatus.CANCELLED);
        assertThat(cancelled).isNotEmpty();
        assertThat(cancelled).allMatch(o -> o.getStatus() == OrderStatus.CANCELLED);
    }

    @Test
    void save_persistsOrder() {
        Order order = orderRepository.save(Order.builder()
                .status(OrderStatus.NEW)
                .totalAmount(new BigDecimal("250.00"))
                .createdAt(LocalDateTime.now())
                .build());

        assertThat(order.getId()).isNotNull();
        assertThat(orderRepository.findById(order.getId())).isPresent();
    }
}
