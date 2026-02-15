package com.nimbleways.springboilerplate.strategies;


import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.enums.ProductType;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// Gère les produits normaux
// Si dispo: on décrémente le stock
// Si rupture avec leadTime: on notifie le délai
@Component
@RequiredArgsConstructor
public class NormalProductStrategy implements ProductProcessingStrategy {

    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    @Override
    public void process(Product product) {
        if (isAvailable(product)) {
            decreaseStock(product);
        } else {
            handleOutOfStock(product);
        }
    }

    @Override
    public boolean supports(String productType) {
        return ProductType.NORMAL.name().equals(productType);
    }

    private boolean isAvailable(Product product) {
        return product.getAvailable() != null && product.getAvailable() > 0;
    }

    private void decreaseStock(Product product) {
        product.setAvailable(product.getAvailable() - 1);
        productRepository.save(product);
    }

    private void handleOutOfStock(Product product) {
        Integer leadTime = product.getLeadTime();
        if (leadTime != null && leadTime > 0) {
            notifyDelay(product, leadTime);
        }
    }

    private void notifyDelay(Product product, int leadTime) {
        product.setLeadTime(leadTime);
        productRepository.save(product);
        notificationService.sendDelayNotification(leadTime, product.getName());
    }
}
