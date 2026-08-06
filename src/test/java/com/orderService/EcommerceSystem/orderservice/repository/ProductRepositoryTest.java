package com.orderService.EcommerceSystem.orderservice.repository;

import com.orderService.EcommerceSystem.orderservice.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    private Product saveProduct(String name, String category) {
        return productRepository.save(Product.builder()
                .name(name)
                .description("Test description")
                .price(new BigDecimal("32.99"))
                .stockQuantity(50)
                .category(category)
                .createdAt(LocalDateTime.now())
                .build());
    }

    @Test
    void findByCategory_returnsMatchingProducts() {
        saveProduct("Test Milk", "Dairy");
        saveProduct("Test Cheese", "Dairy");
        saveProduct("Test Bread", "Bakery");

        List<Product> dairy = productRepository.findByCategory("Dairy");
        assertThat(dairy).isNotEmpty();
        assertThat(dairy).allMatch(p -> p.getCategory().equals("Dairy"));
    }

    @Test
    void findByNameContainingIgnoreCase_returnsMatch() {
        saveProduct("Organic Whole Milk", "Dairy");

        Page<Product> results = productRepository
                .findByNameContainingIgnoreCase("milk", PageRequest.of(0, 10));
        assertThat(results.getContent()).isNotEmpty();
        assertThat(results.getContent()).anyMatch(p -> p.getName().toLowerCase().contains("milk"));
    }

    @Test
    void findByCategory_withPageable_returnsPaged() {
        saveProduct("Paged Dairy Item", "Dairy");

        Page<Product> page = productRepository.findByCategory("Dairy", PageRequest.of(0, 5));
        assertThat(page).isNotNull();
        assertThat(page.getContent()).isNotEmpty();
    }

    @Test
    void save_persistsProduct() {
        Product saved = saveProduct("New Test Product", "Groceries");
        assertThat(saved.getId()).isNotNull();
        assertThat(productRepository.findById(saved.getId())).isPresent();
    }
}
