package org.example.model;

public class InventoryItem {

    private final int productId;
    private final int locationId;
    private final int quantity;

    public InventoryItem(int productId, int locationId, int quantity) {
        this.productId = productId;
        this.locationId = locationId;
        this.quantity = quantity;
    }

    public int getProductId() {
        return productId;
    }

    public int getLocationId() {
        return locationId;
    }

    public int getQuantity() {
        return quantity;
    }
}