package org.example.service;

import org.example.model.StockMovement;

public class StockMovementValidator {

    public void validate(StockMovement movement) {

        if (movement == null) {
            throw new IllegalArgumentException(
                    "Stock movement is required.");
        }

        if (movement.getProductId() <= 0) {
            throw new IllegalArgumentException(
                    "Select a valid product.");
        }

        if (movement.getLocationId() <= 0) {
            throw new IllegalArgumentException(
                    "Select a valid storage location.");
        }

        if (movement.getMovementType() == null) {
            throw new IllegalArgumentException(
                    "Select IN or OUT.");
        }

        if (movement.getQuantity() <= 0) {
            throw new IllegalArgumentException(
                    "Movement quantity must be greater than zero.");
        }

        if (movement.getDate() == null) {
            throw new IllegalArgumentException(
                    "Movement date is required.");
        }
    }
}