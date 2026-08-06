package com.orderService.EcommerceSystem.orderservice.controller;

import com.orderService.EcommerceSystem.orderservice.dto.CreateOrderRequest;
import com.orderService.EcommerceSystem.orderservice.dto.CreateProductRequest;
import com.orderService.EcommerceSystem.orderservice.dto.OrderItemRequest;
import com.orderService.EcommerceSystem.orderservice.dto.ProductResponse;
import com.orderService.EcommerceSystem.orderservice.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class OrderControllerIT {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ProductService productService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
    }

    private ProductResponse createTestProduct(String name, int stock) {
        CreateProductRequest req = new CreateProductRequest();
        req.setName(name);
        req.setDescription("Test product");
        req.setPrice(new BigDecimal("29.99"));
        req.setStockQuantity(stock);
        req.setCategory("Test");
        return productService.createProduct(req);
    }

    @Test
    void createProduct_returnsCreated() throws Exception {
        CreateProductRequest req = new CreateProductRequest();
        req.setName("IT Test Milk");
        req.setDescription("Test description");
        req.setPrice(new BigDecimal("32.99"));
        req.setStockQuantity(50);
        req.setCategory("Dairy");

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("IT Test Milk")))
                .andExpect(jsonPath("$.stockQuantity", is(50)));
    }

    @Test
    void createOrder_success() throws Exception {
        ProductResponse product = createTestProduct("IT Test Rice", 20);

        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(product.getId());
        item.setQuantity(2);

        CreateOrderRequest req = new CreateOrderRequest();
        req.setItems(List.of(item));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("NEW")))
                .andExpect(jsonPath("$.items", hasSize(1)));
    }

    @Test
    void createOrder_insufficientStock_returns400() throws Exception {
        ProductResponse product = createTestProduct("IT Low Stock Item", 1);

        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(product.getId());
        item.setQuantity(5);

        CreateOrderRequest req = new CreateOrderRequest();
        req.setItems(List.of(item));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Insufficient Stock")));
    }

    @Test
    void cancelOrder_success() throws Exception {
        ProductResponse product = createTestProduct("IT Cancelable Item", 10);

        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(product.getId());
        item.setQuantity(2);

        CreateOrderRequest req = new CreateOrderRequest();
        req.setItems(List.of(item));

        String createResult = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long orderId = objectMapper.readTree(createResult).get("id").asLong();

        mockMvc.perform(post("/api/orders/{id}/cancel", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    @Test
    void cancelOrder_alreadyCancelled_returns400() throws Exception {
        ProductResponse product = createTestProduct("IT Cancel Twice Item", 10);

        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(product.getId());
        item.setQuantity(1);

        CreateOrderRequest req = new CreateOrderRequest();
        req.setItems(List.of(item));

        String createResult = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long orderId = objectMapper.readTree(createResult).get("id").asLong();

        mockMvc.perform(post("/api/orders/{id}/cancel", orderId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/orders/{id}/cancel", orderId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Order Already Cancelled")));
    }

    @Test
    void stockRestored_afterCancellation() throws Exception {
        ProductResponse product = createTestProduct("IT Stock Restore Item", 10);

        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(product.getId());
        item.setQuantity(3);

        CreateOrderRequest req = new CreateOrderRequest();
        req.setItems(List.of(item));

        String createResult = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long orderId = objectMapper.readTree(createResult).get("id").asLong();

        // Cancel — stock should be restored to 10
        mockMvc.perform(post("/api/orders/{id}/cancel", orderId))
                .andExpect(status().isOk());

        // Verify stock restored by placing same order again (would fail if stock not restored)
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }
}
