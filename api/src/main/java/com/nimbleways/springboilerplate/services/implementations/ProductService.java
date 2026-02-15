package com.nimbleways.springboilerplate.services.implementations;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;

/**
 * @deprecated Cette classe est obsolète et doit être supprimée
 * On utilise désormais les stratégies (NormalProductStrategy, SeasonalProductStrategy, ExpirableProductStrategy) à la place.
 *
 * Migration path:
 * - Pour NORMAL: NormalProductStrategy
 * - Pour SEASONAL: SeasonalProductStrategy
 * - Pour EXPIRABLE: ExpirableProductStrategy
 */
@Deprecated
@Service
public class ProductService {

    @Autowired
    ProductRepository pr;

    @Autowired
    NotificationService ns;

    /**
     * @deprecated Utilisez NormalProductStrategy.process() à la place
     */
    @Deprecated
    public void notifyDelay(int leadTime, Product p) {
        p.setLeadTime(leadTime);
        pr.save(p);
        ns.sendDelayNotification(leadTime, p.getName());
    }

    /**
     * @deprecated Utilisez NormalProductStrategy.process() à la place
     */
    @Deprecated
    public void handleSeasonalProduct(Product p) {
        if (LocalDate.now().plusDays(p.getLeadTime()).isAfter(p.getSeasonEndDate())) {
            ns.sendOutOfStockNotification(p.getName());
            p.setAvailable(0);
            pr.save(p);
        } else if (p.getSeasonStartDate().isAfter(LocalDate.now())) {
            ns.sendOutOfStockNotification(p.getName());
            pr.save(p);
        } else {
            notifyDelay(p.getLeadTime(), p);
        }
    }

    /**
     * @deprecated Utilisez ExpirableProductStrategy.process() à la place
     */
    @Deprecated
    public void handleExpiredProduct(Product p) {
        if (p.getAvailable() > 0 && p.getExpiryDate().isAfter(LocalDate.now())) {
            p.setAvailable(p.getAvailable() - 1);
            pr.save(p);
        } else {
            ns.sendExpirationNotification(p.getName(), p.getExpiryDate());
            p.setAvailable(0);
            pr.save(p);
        }
    }
}