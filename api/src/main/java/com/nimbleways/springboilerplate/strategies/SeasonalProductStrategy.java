package com.nimbleways.springboilerplate.strategies;


import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.enums.ProductType;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

// Gère les produits saisonniers
// Vérifie qu'on est dans la bonne période ET qu'il y a du stock
// Si le délai de livraison dépasse la fin de saison -> indispo
@Component
@RequiredArgsConstructor
public class SeasonalProductStrategy implements ProductProcessingStrategy {

    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    @Override
    public void process(Product product) {
        if (isInSeasonAndAvailable(product)) {
            decreaseStock(product);
        } else {
            handleUnavailableProduct(product);
        }
    }

    @Override
    public boolean supports(String productType) {
        return ProductType.SEASONAL.name().equals(productType);
    }

    private boolean isInSeasonAndAvailable(Product product) {
        return isInSeason(product) && hasStock(product);
    }

    private boolean isInSeason(Product product) {
        LocalDate now = LocalDate.now();
        return product.getSeasonStartDate() != null
                && product.getSeasonEndDate() != null
                && now.isAfter(product.getSeasonStartDate())
                && now.isBefore(product.getSeasonEndDate());
    }

    private boolean hasStock(Product product) {
        return product.getAvailable() != null && product.getAvailable() > 0;
    }

    private void decreaseStock(Product product) {
        product.setAvailable(product.getAvailable() - 1);
        productRepository.save(product);
    }

    private void handleUnavailableProduct(Product product) {
        if (isBeforeSeasonStart(product)) {
            markAsOutOfStock(product);
        } else if (wouldDeliveryBeAfterSeasonEnd(product)) {
            markAsOutOfStock(product);
        } else {
            notifyDelay(product);
        }
    }

    private boolean isBeforeSeasonStart(Product product) {
        return product.getSeasonStartDate() != null
                && product.getSeasonStartDate().isAfter(LocalDate.now());
    }

    private boolean wouldDeliveryBeAfterSeasonEnd(Product product) {
        if (product.getLeadTime() == null || product.getSeasonEndDate() == null) {
            return false;
        }
        LocalDate estimatedDelivery = LocalDate.now().plusDays(product.getLeadTime());
        return estimatedDelivery.isAfter(product.getSeasonEndDate());
    }

    private void markAsOutOfStock(Product product) {
        notificationService.sendOutOfStockNotification(product.getName());
        product.setAvailable(0);
        productRepository.save(product);
    }

    private void notifyDelay(Product product) {
        Integer leadTime = product.getLeadTime();
        if (leadTime != null && leadTime > 0) {
            product.setLeadTime(leadTime);
            productRepository.save(product);
            notificationService.sendDelayNotification(leadTime, product.getName());
        }
    }
}
