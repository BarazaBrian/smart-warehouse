package org.example.service;

import org.example.model.InventoryItem;

public class InventoryValidator {

    public void validate(InventoryItem item) {

        if (item == null) {
            throw new IllegalArgumentException(
                    "Inventory item is required.");
        }

        if (item.getProductId() <= 0) {
            throw new IllegalArgumentException(
                    "Select a valid product.");
        }

        if (item.getLocationId() <= 0) {
            throw new IllegalArgumentException(
                    "Select a valid storage location.");
        }

        if (item.getQuantity() < 0) {
            throw new IllegalArgumentException(
                    "Quantity cannot be negative.");
        }
    }
}