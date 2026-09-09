package org.example.service;

import org.example.model.MovementType;
import org.example.model.StockMovement;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class StockMovementValidatorTest {

    private final StockMovementValidator validator =
            new StockMovementValidator();

    private final LocalDateTime date =
            LocalDateTime.of(2026, 9, 9, 10, 0);

    @Test
    void acceptsValidStockIn() {
        StockMovement movement =
                new StockMovement(1, 3, MovementType.IN, 10, date);

        assertDoesNotThrow(() -> validator.validate(movement));
    }

    @Test
    void acceptsStockOutOfOneUnit() {
        StockMovement movement =
                new StockMovement(1, 3, MovementType.OUT, 1, date);

        assertDoesNotThrow(() -> validator.validate(movement));
    }

    @Test
    void rejectsMissingMovement() {
        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(null));
    }

    @Test
    void rejectsZeroProductId() {
        StockMovement movement =
                new StockMovement(0, 3, MovementType.IN, 10, date);

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(movement));
    }

    @Test
    void rejectsNegativeProductId() {
        StockMovement movement =
                new StockMovement(-1, 3, MovementType.IN, 10, date);

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(movement));
    }

    @Test
    void rejectsZeroLocationId() {
        StockMovement movement =
                new StockMovement(1, 0, MovementType.IN, 10, date);

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(movement));
    }

    @Test
    void rejectsNegativeLocationId() {
        StockMovement movement =
                new StockMovement(1, -1, MovementType.IN, 10, date);

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(movement));
    }

    @Test
    void rejectsMissingMovementType() {
        StockMovement movement =
                new StockMovement(1, 3, null, 10, date);

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(movement));
    }

    @Test
    void rejectsZeroQuantity() {
        StockMovement movement =
                new StockMovement(1, 3, MovementType.IN, 0, date);

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(movement));
    }

    @Test
    void rejectsNegativeQuantity() {
        StockMovement movement =
                new StockMovement(1, 3, MovementType.OUT, -1, date);

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(movement));
    }

    @Test
    void rejectsMissingDate() {
        StockMovement movement =
                new StockMovement(1, 3, MovementType.IN, 10, null);

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(movement));
    }
}