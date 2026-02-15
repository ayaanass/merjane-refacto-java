package com.nimbleways.springboilerplate.enums;

// Les différents types de produits qu'on gère
public enum ProductType {
    NORMAL,      // Produits normaux
    SEASONAL,    // Produits saisonniers
    EXPIRABLE;   // Produits expirables

    public static ProductType fromString(String type) {
        if (type == null) {
            throw new IllegalArgumentException("Product type cannot be null");
        }

        try {
            return ProductType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown product type: " + type, e);
        }
    }
}
