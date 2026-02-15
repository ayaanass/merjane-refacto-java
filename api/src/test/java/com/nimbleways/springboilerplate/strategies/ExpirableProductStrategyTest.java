package com.nimbleways.springboilerplate.strategies;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;

@ExtendWith(MockitoExtension.class)
class ExpirableProductStrategyTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private NotificationService notificationService;

    private ExpirableProductStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new ExpirableProductStrategy(productRepository, notificationService);
    }

    @Test
    void shouldDecreaseStockWhenAvailableAndNotExpired() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Product product = new Product(1L, 5, 10, "EXPIRABLE", "Fresh Milk",
                tomorrow, null, null);

        strategy.process(product);

        assertEquals(9, product.getAvailable());
        verify(productRepository, times(1)).save(product);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotifyExpirationWhenProductIsExpired() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Product product = new Product(1L, 5, 5, "EXPIRABLE", "Expired Milk",
                yesterday, null, null);

        strategy.process(product);

        assertEquals(0, product.getAvailable());
        verify(productRepository, times(1)).save(product);
        verify(notificationService, times(1))
                .sendExpirationNotification("Expired Milk", yesterday);
    }

    @Test
    void shouldNotifyExpirationWhenOutOfStock() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Product product = new Product(1L, 5, 0, "EXPIRABLE", "Out of Stock Item",
                tomorrow, null, null);

        strategy.process(product);

        assertEquals(0, product.getAvailable());
        verify(notificationService, times(1))
                .sendExpirationNotification("Out of Stock Item", tomorrow);
    }
}
