package org.example.service;

import java.time.LocalDate;

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

    @Test
    void includesProductExpiringToday() {
        LocalDate today = LocalDate.of(2026, 9, 11);

        Product product = new Product(
                "Milk", "M001", BigDecimal.ONE, 5, today);

        assertTrue(product.isNearExpiry(today));
    }

    @Test
    void includesProductExpiringInThirtyDays() {
        LocalDate today = LocalDate.of(2026, 9, 11);

        Product product = new Product(
                "Milk", "M001", BigDecimal.ONE, 5, today.plusDays(30));

        assertTrue(product.isNearExpiry(today));
    }

    @Test
    void excludesProductExpiringInThirtyOneDays() {
        LocalDate today = LocalDate.of(2026, 9, 11);

        Product product = new Product(
                "Milk", "M001", BigDecimal.ONE, 5, today.plusDays(31));

        assertFalse(product.isNearExpiry(today));
    }

    @Test
    void excludesAlreadyExpiredProductFromNearExpiry() {
        LocalDate today = LocalDate.of(2026, 9, 11);

        Product product = new Product(
                "Milk", "M001", BigDecimal.ONE, 5, today.minusDays(1));

        assertFalse(product.isNearExpiry(today));
    }

    @Test
    void excludesProductWithoutExpiryDate() {
        LocalDate today = LocalDate.of(2026, 9, 11);

        Product product = new Product(
                "Desk", "D001", BigDecimal.ONE, 5, null);

        assertFalse(product.isNearExpiry(today));
    }

    @Test
    void detectsStockBelowMinimum() {
        Product product = createProduct(
                "Water", "W001", BigDecimal.ONE, 10);

        assertTrue(product.isLowStock(9));
    }

    @Test
    void acceptsStockEqualToMinimum() {
        Product product = createProduct(
                "Water", "W001", BigDecimal.ONE, 10);

        assertFalse(product.isLowStock(10));
    }

    @Test
    void acceptsStockAboveMinimum() {
        Product product = createProduct(
                "Water", "W001", BigDecimal.ONE, 10);

        assertFalse(product.isLowStock(11));
    }

    @Test
    void detectsEmptyStockWhenMinimumIsPositive() {
        Product product = createProduct(
                "Water", "W001", BigDecimal.ONE, 10);

        assertTrue(product.isLowStock(0));
    }

    @Test
    void acceptsEmptyStockWhenMinimumIsZero() {
        Product product = createProduct(
                "Water", "W001", BigDecimal.ONE, 0);

        assertFalse(product.isLowStock(0));
    }

    @Test
    void rejectsNegativeTotalStock() {
        Product product = createProduct(
                "Water", "W001", BigDecimal.ONE, 10);

        assertThrows(IllegalArgumentException.class,
                () -> product.isLowStock(-1));
    }
}