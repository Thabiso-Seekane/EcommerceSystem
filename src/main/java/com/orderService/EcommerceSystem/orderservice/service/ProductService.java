package com.orderService.EcommerceSystem.orderservice.service;

import com.orderService.EcommerceSystem.orderservice.dto.CreateProductRequest;
import com.orderService.EcommerceSystem.orderservice.dto.ProductResponse;
import com.orderService.EcommerceSystem.orderservice.entity.Product;
import com.orderService.EcommerceSystem.orderservice.exception.ResourceNotFoundException;
import com.orderService.EcommerceSystem.orderservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .category(request.getCategory())
                .createdAt(LocalDateTime.now())
                .build();

        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        return ProductResponse.from(findProductById(id));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> listProducts(String category, String name, Pageable pageable) {
        if (category != null && name != null) {
            return productRepository
                    .findByCategoryAndNameContainingIgnoreCase(category, name, pageable)
                    .map(ProductResponse::from);
        }
        if (category != null) {
            return productRepository.findByCategory(category, pageable).map(ProductResponse::from);
        }
        if (name != null) {
            return productRepository.findByNameContainingIgnoreCase(name, pageable).map(ProductResponse::from);
        }
        return productRepository.findAll(pageable).map(ProductResponse::from);
    }

    // Package-visible helper used by OrderService
    Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }
}
