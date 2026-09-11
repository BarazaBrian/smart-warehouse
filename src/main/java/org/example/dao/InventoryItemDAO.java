package org.example.dao;

import org.example.model.InventoryItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

public class InventoryItemDAO {

    private final Connection connection;

    public InventoryItemDAO(Connection connection) {
        this.connection = connection;
    }

    // TODO (Elera): Create the inventory_items table at startup.
    // Required columns:
    // product_id  - INT, foreign key to products.id
    // location_id - INT, foreign key to the storage-location table
    // quantity    - INT, not null, must be zero or greater
    // Make (product_id, location_id) the combined primary key.
    // Use ON DELETE RESTRICT for both foreign keys.

    public InventoryItem findItem(int productId, int locationId)
            throws SQLException {

        String sql = """
                SELECT product_id, location_id, quantity
                FROM inventory_items
                WHERE product_id = ? AND location_id = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, productId);
            statement.setInt(2, locationId);

            try (ResultSet results = statement.executeQuery()) {

                if (results.next()) {
                    return new InventoryItem(
                            results.getInt("product_id"),
                            results.getInt("location_id"),
                            results.getInt("quantity")
                    );
                }
            }
        }

        return null;
    }
    public void assignProductToLocation(int productId, int locationId)
            throws SQLException {

        String sql = """
            INSERT INTO inventory_items
                (product_id, location_id, quantity)
            VALUES (?, ?, 0)
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, productId);
            statement.setInt(2, locationId);

            statement.executeUpdate();
        }
    }
    public boolean updateQuantity(int productId, int locationId,
                                  int expectedQuantity, int newQuantity)
            throws SQLException {

        // Call this within the same transaction that saves the stock movement.
        String sql = """
            UPDATE inventory_items
            SET quantity = ?
            WHERE product_id = ?
              AND location_id = ?
              AND quantity = ?
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, newQuantity);
            statement.setInt(2, productId);
            statement.setInt(3, locationId);
            statement.setInt(4, expectedQuantity);

            return statement.executeUpdate() > 0;
        }
    }

    public List<InventoryItem> getAllItems() throws SQLException {

        List<InventoryItem> items = new ArrayList<>();

        String sql = """
            SELECT product_id, location_id, quantity
            FROM inventory_items
            ORDER BY product_id, location_id
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

            while (results.next()) {
                InventoryItem item = new InventoryItem(
                        results.getInt("product_id"),
                        results.getInt("location_id"),
                        results.getInt("quantity")
                );

                items.add(item);
            }
        }

        return items;
    }

    public long getTotalQuantity(int productId) throws SQLException {

        String sql = """
            SELECT COALESCE(SUM(quantity), 0) AS total_quantity
            FROM inventory_items
            WHERE product_id = ?
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, productId);

            try (ResultSet results = statement.executeQuery()) {
                results.next();
                return results.getLong("total_quantity");
            }
        }
    }
}