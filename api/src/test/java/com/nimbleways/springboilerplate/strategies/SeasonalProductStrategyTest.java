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
class SeasonalProductStrategyTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private NotificationService notificationService;

    private SeasonalProductStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new SeasonalProductStrategy(productRepository, notificationService);
    }

    @Test
    void shouldDecreaseStockWhenInSeasonAndAvailable() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Product product = new Product(1L, 5, 10, "SEASONAL", "Summer Hat",
                null, yesterday, tomorrow);

        strategy.process(product);

        assertEquals(9, product.getAvailable());
        verify(productRepository, times(1)).save(product);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotifyOutOfStockWhenBeforeSeasonStart() {
        LocalDate nextWeek = LocalDate.now().plusDays(7);
        LocalDate nextMonth = LocalDate.now().plusDays(30);
        Product product = new Product(1L, 5, 0, "SEASONAL", "Winter Jacket",
                null, nextWeek, nextMonth);

        strategy.process(product);

        assertEquals(0, product.getAvailable());
        verify(productRepository, times(1)).save(product);
        verify(notificationService, times(1))
                .sendOutOfStockNotification("Winter Jacket");
    }

    @Test
    void shouldNotifyOutOfStockWhenDeliveryWouldBeAfterSeasonEnd() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate in5Days = LocalDate.now().plusDays(5);
        Product product = new Product(1L, 10, 0, "SEASONAL", "Spring Flower",
                null, yesterday, in5Days);

        strategy.process(product);

        assertEquals(0, product.getAvailable());
        verify(notificationService, times(1))
                .sendOutOfStockNotification("Spring Flower");
    }

    @Test
    void shouldNotifyDelayWhenInSeasonButOutOfStock() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate in30Days = LocalDate.now().plusDays(30);
        Product product = new Product(1L, 5, 0, "SEASONAL", "Seasonal Item",
                null, yesterday, in30Days);

        strategy.process(product);

        verify(notificationService, times(1))
                .sendDelayNotification(5, "Seasonal Item");
    }
}
