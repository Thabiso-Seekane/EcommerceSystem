package com.orderService.EcommerceSystem.orderservice.service;

import com.orderService.EcommerceSystem.orderservice.dto.CreateOrderRequest;
import com.orderService.EcommerceSystem.orderservice.dto.OrderResponse;
import com.orderService.EcommerceSystem.orderservice.entity.*;
import com.orderService.EcommerceSystem.orderservice.exception.InsufficientStockException;
import com.orderService.EcommerceSystem.orderservice.exception.OrderAlreadyCancelledException;
import com.orderService.EcommerceSystem.orderservice.exception.ResourceNotFoundException;
import com.orderService.EcommerceSystem.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;

    @Transactional
    public OrderResponse placeOrder(CreateOrderRequest request) {
        Order order = Order.builder()
                .status(OrderStatus.NEW)
                .createdAt(LocalDateTime.now())
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (var itemRequest : request.getItems()) {
            Product product = productService.findProductById(itemRequest.getProductId());

            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product '%s'. Available: %d, requested: %d"
                                .formatted(product.getName(), product.getStockQuantity(), itemRequest.getQuantity())
                );
            }

            BigDecimal subtotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            OrderItem item = OrderItem.builder()
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .price(product.getPrice())
                    .subtotal(subtotal)
                    .build();

            order.getItems().add(item);
            item.setOrder(order);

            // Deduct stock
            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());

            total = total.add(subtotal);
        }

        order.setTotalAmount(total);
        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        Order order = findOrderById(orderId);

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new OrderAlreadyCancelledException(
                    "Order with id " + orderId + " is already cancelled"
            );
        }

        // Restore stock for each item
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
        }

        order.setStatus(OrderStatus.CANCELLED);
        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        return OrderResponse.from(findOrderById(orderId));
    }

    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
    }
}
