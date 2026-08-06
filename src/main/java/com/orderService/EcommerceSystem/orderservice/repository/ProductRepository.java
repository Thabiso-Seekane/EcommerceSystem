package com.orderService.EcommerceSystem.orderservice.repository;

import com.orderService.EcommerceSystem.orderservice.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategory(String category);

    Page<Product> findByCategory(String category, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Product> findByCategoryAndNameContainingIgnoreCase(String category, String name, Pageable pageable);

    @Query(value = """
            SELECT p.id, p.name, p.category, p.price, p.stock_quantity,
                   SUM(oi.quantity) AS total_sold
            FROM products p
            JOIN order_items oi ON oi.product_id = p.id
            JOIN orders o ON o.id = oi.order_id
            WHERE o.created_at BETWEEN :startDate AND :endDate
            GROUP BY p.id, p.name, p.category, p.price, p.stock_quantity
            ORDER BY total_sold DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findTopSellingProducts(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate,
            @Param("limit") int limit);
}
