package com.orderService.EcommerceSystem.orderservice.service;

import com.orderService.EcommerceSystem.orderservice.dto.TopProductResponse;
import com.orderService.EcommerceSystem.orderservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<TopProductResponse> getTopSellingProducts(LocalDate startDate, LocalDate endDate, int limit) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<Object[]> rows = productRepository.findTopSellingProducts(start, end, limit);

        return rows.stream().map(row -> new TopProductResponse(
                ((Number) row[0]).longValue(),        // id
                (String) row[1],                       // name
                (String) row[2],                       // category
                (BigDecimal) row[3],                   // price
                ((Number) row[4]).intValue(),          // stock_quantity
                ((Number) row[5]).longValue()          // total_sold
        )).collect(Collectors.toList());
    }
}
