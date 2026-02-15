package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.strategies.ProductProcessingStrategy;
import com.nimbleways.springboilerplate.strategies.ProductStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

/**
 *  Service qui orchestre le traitement des commandes
 */
@Service
@RequiredArgsConstructor
public class OrderProcessingService {

    private final OrderRepository orderRepository;
    private final ProductStrategyFactory strategyFactory;

    @Transactional
    public Order processOrder(Long orderId) {
        Order order = findOrder(orderId);
        processOrderItems(order);
        return order;
    }

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order not found with id: " + orderId
                ));
    }

    private void processOrderItems(Order order) {
        Set<Product> products = order.getItems();
        if (products == null || products.isEmpty()) {
            return;
        }

        for (Product product : products) {
            processProduct(product);
        }
    }

    private void processProduct(Product product) {
        ProductProcessingStrategy strategy = strategyFactory.getStrategy(product);
        strategy.process(product);
    }
}
