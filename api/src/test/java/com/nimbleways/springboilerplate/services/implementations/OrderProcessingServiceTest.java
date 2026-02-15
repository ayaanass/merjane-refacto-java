package com.nimbleways.springboilerplate.services.implementations;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.strategies.ProductProcessingStrategy;
import com.nimbleways.springboilerplate.strategies.ProductStrategyFactory;

@ExtendWith(MockitoExtension.class)
class OrderProcessingServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductStrategyFactory strategyFactory;

    @Mock
    private ProductProcessingStrategy productStrategy;

    private OrderProcessingService service;

    @BeforeEach
    void setUp() {
        service = new OrderProcessingService(orderRepository, strategyFactory);
    }

    @Test
    void shouldProcessOrderSuccessfully() {
        Long orderId = 1L;
        Product product = new Product(1L, 10, 5, "NORMAL", "Test Product",
                null, null, null);
        Order order = new Order(orderId, Set.of(product));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(strategyFactory.getStrategy(product)).thenReturn(productStrategy);

        Order result = service.processOrder(orderId);

        assertNotNull(result);
        assertEquals(orderId, result.getId());
        verify(productStrategy, times(1)).process(product);
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFound() {
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.processOrder(orderId)
        );

        assertTrue(exception.getMessage().contains("Order not found"));
    }

    @Test
    void shouldHandleOrderWithNoItems() {
        Long orderId = 1L;
        Order order = new Order(orderId, null);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Order result = service.processOrder(orderId);

        assertNotNull(result);
        verifyNoInteractions(strategyFactory);
    }
}
