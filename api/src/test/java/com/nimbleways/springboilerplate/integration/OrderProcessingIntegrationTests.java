package com.nimbleways.springboilerplate.integration;


import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.OrderProcessingService;

@SpringBootTest
@Transactional
class OrderProcessingIntegrationTests {

    @Autowired
    private OrderProcessingService orderProcessingService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    void shouldProcessNormalProductOrder() {
        Product product = new Product(null, 10, 5, "NORMAL", "RJ45 Cable",
                null, null, null);
        product = productRepository.save(product);

        Order order = new Order(null, Set.of(product));
        order = orderRepository.save(order);

        orderProcessingService.processOrder(order.getId());

        Product updatedProduct = productRepository.findById(product.getId()).get();
        assertEquals(4, updatedProduct.getAvailable());
    }

    @Test
    void shouldProcessSeasonalProductInSeason() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Product product = new Product(null, 5, 10, "SEASONAL", "Summer Hat",
                null, yesterday, tomorrow);
        product = productRepository.save(product);

        Order order = new Order(null, Set.of(product));
        order = orderRepository.save(order);

        orderProcessingService.processOrder(order.getId());

        Product updatedProduct = productRepository.findById(product.getId()).get();
        assertEquals(9, updatedProduct.getAvailable());
    }

    @Test
    void shouldProcessExpirableProductNotExpired() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Product product = new Product(null, 5, 10, "EXPIRABLE", "Fresh Milk",
                tomorrow, null, null);
        product = productRepository.save(product);

        Order order = new Order(null, Set.of(product));
        order = orderRepository.save(order);

        orderProcessingService.processOrder(order.getId());

        Product updatedProduct = productRepository.findById(product.getId()).get();
        assertEquals(9, updatedProduct.getAvailable());
    }

    @Test
    void shouldProcessOrderWithMultipleProducts() {
        Product normalProduct = new Product(null, 10, 5, "NORMAL", "Cable",
                null, null, null);
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Product expirableProduct = new Product(null, 5, 3, "EXPIRABLE", "Milk",
                tomorrow, null, null);

        normalProduct = productRepository.save(normalProduct);
        expirableProduct = productRepository.save(expirableProduct);

        Order order = new Order(null, Set.of(normalProduct, expirableProduct));
        order = orderRepository.save(order);

        orderProcessingService.processOrder(order.getId());

        Product updatedNormal = productRepository.findById(normalProduct.getId()).get();
        Product updatedExpirable = productRepository.findById(expirableProduct.getId()).get();

        assertEquals(4, updatedNormal.getAvailable());
        assertEquals(2, updatedExpirable.getAvailable());
    }
}
