package org.example.service;

import org.example.model.Product;
import java.math.BigDecimal;

public class ProductValidator {

    public void validate(Product product) {

        if (product == null) {
            throw new IllegalArgumentException("Product is required.");
        }

        if (product.getName() == null
                || product.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "Product name is required.");
        }

        if (product.getSku() == null
                || product.getSku().isBlank()) {
            throw new IllegalArgumentException(
                    "SKU is required.");
        }

        if (product.getUnitPrice() == null
                || product.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Unit price must be zero or greater.");
        }

        if (product.getMinimumStockLevel() < 0) {
            throw new IllegalArgumentException(
                    "Minimum stock level must be zero or greater.");
        }
    }
}