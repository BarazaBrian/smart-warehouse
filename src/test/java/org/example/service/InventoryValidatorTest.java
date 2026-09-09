package org.example.service;

import org.example.model.InventoryItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryValidatorTest {

    private final InventoryValidator validator = new InventoryValidator();

    @Test
    void acceptsValidInventoryItem() {
        InventoryItem item = new InventoryItem(1, 3, 20);

        assertDoesNotThrow(() -> validator.validate(item));
    }

    @Test
    void acceptsZeroQuantity() {
        InventoryItem item = new InventoryItem(1, 3, 0);

        assertDoesNotThrow(() -> validator.validate(item));
    }

    @Test
    void rejectsMissingInventoryItem() {
        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(null));
    }

    @Test
    void rejectsZeroProductId() {
        InventoryItem item = new InventoryItem(0, 3, 20);

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(item));
    }

    @Test
    void rejectsNegativeProductId() {
        InventoryItem item = new InventoryItem(-1, 3, 20);

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(item));
    }

    @Test
    void rejectsZeroLocationId() {
        InventoryItem item = new InventoryItem(1, 0, 20);

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(item));
    }

    @Test
    void rejectsNegativeLocationId() {
        InventoryItem item = new InventoryItem(1, -1, 20);

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(item));
    }

    @Test
    void rejectsNegativeQuantity() {
        InventoryItem item = new InventoryItem(1, 3, -1);

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(item));
    }
}