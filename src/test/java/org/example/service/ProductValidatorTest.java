package org.example.service;

import org.example.model.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class ProductValidatorTest {

    private final ProductValidator validator = new ProductValidator();

    private Product createProduct(String name, String sku,
                                  BigDecimal price, int minimumStock) {
        return new Product(name, sku, price, minimumStock, null);
    }

    @Test
    void acceptsValidProduct() {
        Product product = createProduct(
                "Water", "W001", new BigDecimal("25.00"), 10);

        assertDoesNotThrow(() -> validator.validate(product));
    }

    @Test
    void acceptsZeroPriceAndZeroMinimumStock() {
        Product product = createProduct(
                "Water", "W001", BigDecimal.ZERO, 0);

        assertDoesNotThrow(() -> validator.validate(product));
    }

    @Test
    void rejectsMissingProduct() {
        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(null));
    }

    @Test
    void rejectsBlankName() {
        Product product = createProduct(
                "   ", "W001", BigDecimal.ONE, 10);

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(product));
    }

    @Test
    void rejectsMissingName() {
        Product product = createProduct(
                null, "W001", BigDecimal.ONE, 10);

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(product));
    }

    @Test
    void rejectsBlankSku() {
        Product product = createProduct(
                "Water", "   ", BigDecimal.ONE, 10);

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(product));
    }

    @Test
    void rejectsMissingSku() {
        Product product = createProduct(
                "Water", null, BigDecimal.ONE, 10);

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(product));
    }

    @Test
    void rejectsNegativePrice() {
        Product product = createProduct(
                "Water", "W001", new BigDecimal("-0.01"), 10);

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(product));
    }

    @Test
    void rejectsMissingPrice() {
        Product product = createProduct(
                "Water", "W001", null, 10);

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(product));
    }

    @Test
    void rejectsNegativeMinimumStock() {
        Product product = createProduct(
                "Water", "W001", BigDecimal.ONE, -1);

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(product));
    }
}