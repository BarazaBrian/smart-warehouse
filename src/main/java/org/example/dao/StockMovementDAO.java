package org.example.dao;

import org.example.model.StockMovement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class StockMovementDAO {

    private final Connection connection;

    public StockMovementDAO(Connection connection) {
        this.connection = connection;
    }

    // TODO (Elera): Create stock_movements automatically at startup.
    // Required columns:
    // id            - INT, auto-increment primary key
    // product_id    - INT, not null
    // location_id   - INT, not null
    // movement_type - VARCHAR(3), allows only IN or OUT, not null
    // quantity      - INT, greater than zero, not null
    // movement_date - DATETIME, not null
    // Add a combined foreign key (product_id, location_id)
    // referencing inventory_items(product_id, location_id).
    // Use ON DELETE RESTRICT.

    public void addMovement(StockMovement movement)
            throws SQLException {

        String sql = """
                INSERT INTO stock_movements
                    (product_id, location_id, movement_type,
                     quantity, movement_date)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, movement.getProductId());
            statement.setInt(2, movement.getLocationId());
            statement.setString(3, movement.getMovementType().name());
            statement.setInt(4, movement.getQuantity());
            statement.setTimestamp(
                    5,
                    Timestamp.valueOf(movement.getDate())
            );

            statement.executeUpdate();
        }
    }
}