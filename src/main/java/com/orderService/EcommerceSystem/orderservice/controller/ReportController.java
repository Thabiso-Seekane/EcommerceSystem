package com.orderService.EcommerceSystem.orderservice.controller;

import com.orderService.EcommerceSystem.orderservice.dto.TopProductResponse;
import com.orderService.EcommerceSystem.orderservice.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * GET /api/reports/top-products?startDate=2024-01-01&endDate=2024-12-31&limit=10
     */
    @GetMapping("/top-products")
    public List<TopProductResponse> getTopSellingProducts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "10") int limit) {
        return reportService.getTopSellingProducts(startDate, endDate, limit);
    }
}
