package com.nimbleways.springboilerplate.strategies;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;

@ExtendWith(MockitoExtension.class)
class NormalProductStrategyTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private NotificationService notificationService;

    private NormalProductStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new NormalProductStrategy(productRepository, notificationService);
    }

    @Test
    void shouldSupportNormalProductType() {
        assertTrue(strategy.supports("NORMAL"));
        assertFalse(strategy.supports("SEASONAL"));
    }

    @Test
    void shouldDecreaseStockWhenProductIsAvailable() {
        Product product = new Product(1L, 10, 5, "NORMAL", "Test Product",
                null, null, null);

        strategy.process(product);

        assertEquals(4, product.getAvailable());
        verify(productRepository, times(1)).save(product);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotifyDelayWhenProductIsOutOfStockWithLeadTime() {
        Product product = new Product(1L, 15, 0, "NORMAL", "RJ45 Cable",
                null, null, null);

        strategy.process(product);

        assertEquals(0, product.getAvailable());
        assertEquals(15, product.getLeadTime());
        verify(productRepository, times(1)).save(product);
        verify(notificationService, times(1))
                .sendDelayNotification(15, "RJ45 Cable");
    }

    @Test
    void shouldNotNotifyWhenProductIsOutOfStockWithNoLeadTime() {
        Product product = new Product(1L, 0, 0, "NORMAL", "Test Product",
                null, null, null);

        strategy.process(product);

        verifyNoInteractions(productRepository);
        verifyNoInteractions(notificationService);
    }
}