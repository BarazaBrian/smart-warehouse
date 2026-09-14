package org.example.service;

import org.example.model.MovementType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StockCalculatorTest {

    private final StockCalculator calculator = new StockCalculator();

    @Test
    void stockInIncreasesQuantity() {
        int result = calculator.calculateNewQuantity(
                20, MovementType.IN, 10);

        assertEquals(30, result);
    }

    @Test
    void stockOutDecreasesQuantity() {
        int result = calculator.calculateNewQuantity(
                20, MovementType.OUT, 4);

        assertEquals(16, result);
    }

    @Test
    void allowsRemovingAllAvailableStock() {
        int result = calculator.calculateNewQuantity(
                20, MovementType.OUT, 20);

        assertEquals(0, result);
    }

    @Test
    void allowsStockInToEmptyInventory() {
        int result = calculator.calculateNewQuantity(
                0, MovementType.IN, 5);

        assertEquals(5, result);
    }

    @Test
    void rejectsStockOutAboveAvailableQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateNewQuantity(
                        20, MovementType.OUT, 21));
    }

    @Test
    void rejectsStockOutFromEmptyInventory() {
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateNewQuantity(
                        0, MovementType.OUT, 1));
    }

    @Test
    void rejectsNegativeCurrentQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateNewQuantity(
                        -1, MovementType.IN, 5));
    }

    @Test
    void rejectsMissingMovementType() {
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateNewQuantity(20, null, 5));
    }

    @Test
    void rejectsZeroMovementQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateNewQuantity(
                        20, MovementType.IN, 0));
    }

    @Test
    void rejectsNegativeMovementQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateNewQuantity(
                        20, MovementType.OUT, -1));
    }

    @Test
    void allowsStockToReachMaximumInteger() {
        int result = calculator.calculateNewQuantity(
                Integer.MAX_VALUE - 1, MovementType.IN, 1);

        assertEquals(Integer.MAX_VALUE, result);
    }

    @Test
    void rejectsStockInThatWouldOverflow() {
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateNewQuantity(
                        Integer.MAX_VALUE, MovementType.IN, 1));
    }
}