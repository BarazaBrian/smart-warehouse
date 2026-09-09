package org.example.model;

import java.time.LocalDateTime;

public class StockMovement {

    private final int productId;
    private final int locationId;
    private final MovementType movementType;
    private final int quantity;
    private final LocalDateTime date;

    public StockMovement(int productId, int locationId,
                         MovementType movementType, int quantity,
                         LocalDateTime date) {
        this.productId = productId;
        this.locationId = locationId;
        this.movementType = movementType;
        this.quantity = quantity;
        this.date = date;
    }

    public int getProductId() {
        return productId;
    }

    public int getLocationId() {
        return locationId;
    }

    public MovementType getMovementType() {
        return movementType;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDateTime getDate() {
        return date;
    }
}