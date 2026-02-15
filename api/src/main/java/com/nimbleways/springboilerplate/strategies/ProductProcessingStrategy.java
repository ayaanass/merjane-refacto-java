package com.nimbleways.springboilerplate.strategies;


import com.nimbleways.springboilerplate.entities.Product;

// Interface pour traiter les différents types de produits
public interface ProductProcessingStrategy {

    // Traite un produit lors d'une commande
    void process(Product product);

    // Vérifie si cette stratégie gère ce type de produit
    boolean supports(String productType);
}
