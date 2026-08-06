package com.orderService.EcommerceSystem.orderservice.dto;

import com.orderService.EcommerceSystem.orderservice.entity.Order;
import com.orderService.EcommerceSystem.orderservice.entity.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class OrderResponse {

    private Long id;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;

    public static OrderResponse from(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setCreatedAt(order.getCreatedAt());
        response.setItems(
                order.getItems().stream()
                        .map(OrderItemResponse::from)
                        .collect(Collectors.toList())
        );
        return response;
    }
}
