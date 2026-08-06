package com.orderService.EcommerceSystem.orderservice.service;

import com.orderService.EcommerceSystem.orderservice.dto.CreateOrderRequest;
import com.orderService.EcommerceSystem.orderservice.dto.OrderItemRequest;
import com.orderService.EcommerceSystem.orderservice.dto.OrderResponse;
import com.orderService.EcommerceSystem.orderservice.entity.*;
import com.orderService.EcommerceSystem.orderservice.exception.InsufficientStockException;
import com.orderService.EcommerceSystem.orderservice.exception.OrderAlreadyCancelledException;
import com.orderService.EcommerceSystem.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderService orderService;

    private Product milk;
    private Product bread;

    @BeforeEach
    void setUp() {
        milk = Product.builder()
                .id(1L)
                .name("Full Cream Milk 2L")
                .price(new BigDecimal("32.99"))
                .stockQuantity(10)
                .category("Dairy")
                .createdAt(LocalDateTime.now())
                .build();

        bread = Product.builder()
                .id(2L)
                .name("Brown Bread")
                .price(new BigDecimal("18.99"))
                .stockQuantity(5)
                .category("Bakery")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void placeOrder_success_createsOrderAndDeductsStock() {
        when(productService.findProductById(1L)).thenReturn(milk);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o = Order.builder()
                    .id(100L)
                    .status(o.getStatus())
                    .totalAmount(o.getTotalAmount())
                    .createdAt(o.getCreatedAt())
                    .items(o.getItems())
                    .build();
            return o;
        });

        CreateOrderRequest request = new CreateOrderRequest();
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(3);
        request.setItems(List.of(item));

        OrderResponse response = orderService.placeOrder(request);

        assertThat(response).isNotNull();
        assertThat(milk.getStockQuantity()).isEqualTo(7); // 10 - 3
    }

    @Test
    void placeOrder_insufficientStock_throws() {
        when(productService.findProductById(2L)).thenReturn(bread);

        CreateOrderRequest request = new CreateOrderRequest();
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(2L);
        item.setQuantity(10); // only 5 in stock
        request.setItems(List.of(item));

        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Brown Bread");
    }

    @Test
    void cancelOrder_success_restoresStock() {
        OrderItem orderItem = OrderItem.builder()
                .id(1L)
                .product(milk)
                .quantity(3)
                .price(milk.getPrice())
                .subtotal(milk.getPrice().multiply(BigDecimal.valueOf(3)))
                .build();

        List<OrderItem> items = new ArrayList<>();
        items.add(orderItem);

        Order existingOrder = Order.builder()
                .id(100L)
                .status(OrderStatus.NEW)
                .totalAmount(new BigDecimal("98.97"))
                .createdAt(LocalDateTime.now())
                .items(items)
                .build();
        orderItem.setOrder(existingOrder);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(existingOrder);

        orderService.cancelOrder(100L);

        assertThat(existingOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(milk.getStockQuantity()).isEqualTo(13); // 10 + 3 restored
    }

    @Test
    void cancelOrder_alreadyCancelled_throws() {
        Order cancelledOrder = Order.builder()
                .id(200L)
                .status(OrderStatus.CANCELLED)
                .totalAmount(BigDecimal.TEN)
                .createdAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();

        when(orderRepository.findById(200L)).thenReturn(Optional.of(cancelledOrder));

        assertThatThrownBy(() -> orderService.cancelOrder(200L))
                .isInstanceOf(OrderAlreadyCancelledException.class);
    }
}
