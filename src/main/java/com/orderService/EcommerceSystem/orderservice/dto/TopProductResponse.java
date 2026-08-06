package com.orderService.EcommerceSystem.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TopProductResponse {

    private Long id;
    private String name;
    private String category;
    private BigDecimal price;
    private Integer stockQuantity;
    private Long totalSold;
}
