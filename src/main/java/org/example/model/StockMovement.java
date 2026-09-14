package org.example.model;

import java.time.LocalDateTime;

public class StockMovement {
    private int id;
    private final int productId;
    private final int locationId;
    private final MovementType movementType;
    private final int quantity;
    private final LocalDateTime date;

    // Used when creating a new movement before saving.
    public StockMovement(int productId, int locationId,
                         MovementType movementType, int quantity,
                         LocalDateTime date) {
        this.productId = productId;
        this.locationId = locationId;
        this.movementType = movementType;
        this.quantity = quantity;
        this.date = date;
    }

    // Used when loading a saved movement with its database ID.
    public StockMovement(int id, int productId, int locationId,
                         MovementType movementType, int quantity,
                         LocalDateTime date) {
        this(productId, locationId, movementType, quantity, date);
        this.id = id;
    }

    public int getId() {
        return id;
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