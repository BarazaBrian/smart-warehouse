package org.example.service;

import org.example.model.MovementType;

public class StockCalculator {

    public int calculateNewQuantity(int currentQuantity,
                                    MovementType type,
                                    int movementQuantity) {

        if (currentQuantity < 0) {
            throw new IllegalArgumentException(
                    "Current stock cannot be negative.");
        }

        if (type == null) {
            throw new IllegalArgumentException(
                    "Select IN or OUT.");
        }

        if (movementQuantity <= 0) {
            throw new IllegalArgumentException(
                    "Movement quantity must be greater than zero.");
        }

        if (type == MovementType.IN) {

            if (movementQuantity > Integer.MAX_VALUE - currentQuantity) {
                throw new IllegalArgumentException(
                        "The resulting stock quantity is too large.");
            }

            return currentQuantity + movementQuantity;
        }

        if (movementQuantity > currentQuantity) {
            throw new IllegalArgumentException(
                    "There is not enough stock for this movement.");
        }

        return currentQuantity - movementQuantity;
    }
}