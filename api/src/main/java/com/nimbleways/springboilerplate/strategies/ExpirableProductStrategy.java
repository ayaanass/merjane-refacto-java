package com.nimbleways.springboilerplate.strategies;


import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.enums.ProductType;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

// Gère les produits périssables
// Check la date d'expiration avant de vendre
@Component
@RequiredArgsConstructor
public class ExpirableProductStrategy implements ProductProcessingStrategy {

    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    @Override
    public void process(Product product) {
        if (isAvailableAndNotExpired(product)) {
            decreaseStock(product);
        } else {
            handleExpiredOrUnavailableProduct(product);
        }
    }

    @Override
    public boolean supports(String productType) {
        return ProductType.EXPIRABLE.name().equals(productType);
    }

    private boolean isAvailableAndNotExpired(Product product) {
        return hasStock(product) && isNotExpired(product);
    }

    private boolean hasStock(Product product) {
        return product.getAvailable() != null && product.getAvailable() > 0;
    }

    private boolean isNotExpired(Product product) {
        return product.getExpiryDate() != null
                && product.getExpiryDate().isAfter(LocalDate.now());
    }

    private void decreaseStock(Product product) {
        product.setAvailable(product.getAvailable() - 1);
        productRepository.save(product);
    }

    private void handleExpiredOrUnavailableProduct(Product product) {
        notificationService.sendExpirationNotification(
                product.getName(),
                product.getExpiryDate()
        );
        product.setAvailable(0);
        productRepository.save(product);
    }
}
