package com.nimbleways.springboilerplate.strategies;


import com.nimbleways.springboilerplate.entities.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

// Factory pour choisir la bonne stratégie selon le type de produit
@Component
public class ProductStrategyFactory {

    private final List<ProductProcessingStrategy> strategies;

    @Autowired
    public ProductStrategyFactory(List<ProductProcessingStrategy> strategies) {
        this.strategies = strategies;
    }

    public ProductProcessingStrategy getStrategy(Product product) {
        if (product == null || product.getType() == null) {
            throw new IllegalArgumentException("Product and product type must not be null");
        }

        return strategies.stream()
                .filter(strategy -> strategy.supports(product.getType()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No strategy found for product type: " + product.getType()
                ));
    }
}
