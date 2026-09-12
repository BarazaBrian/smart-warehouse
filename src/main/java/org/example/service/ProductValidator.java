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

        if (product.getName().length() > 100) {
            throw new IllegalArgumentException(
                    "Product name must not exceed 100 characters.");
        }

        if (product.getSku().length() > 50) {
            throw new IllegalArgumentException(
                    "SKU must not exceed 50 characters.");
        }

        BigDecimal maximumPrice = new BigDecimal("9999999999.99");

        if (product.getUnitPrice().compareTo(maximumPrice) > 0) {
            throw new IllegalArgumentException(
                    "Unit price must not exceed 9,999,999,999.99.");
        }

        if (product.getUnitPrice().stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException(
                    "Unit price must not require more than two decimal places.");
        }

        if (product.getExpiryDate() != null) {
            int year = product.getExpiryDate().getYear();

            if (year < 1000 || year > 9999) {
                throw new IllegalArgumentException(
                        "Expiry year must be between 1000 and 9999.");
            }
        }
    }
}