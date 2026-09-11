package org.example.service;

import org.example.dao.InventoryItemDAO;
import org.example.dao.StockMovementDAO;
import org.example.model.InventoryItem;
import org.example.model.StockMovement;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;


public class StockMovementService {

    private final DataSource dataSource;

    private final StockMovementValidator validator =
            new StockMovementValidator();

    private final StockCalculator calculator = new StockCalculator();

    // TODO (Elera): Supply a configured MySQL DataSource.
    public StockMovementService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void recordMovement(StockMovement movement)
            throws SQLException {

        validator.validate(movement);

        try (Connection connection = dataSource.getConnection()) {
            recordMovement(connection, movement);
        }
    }

    private void recordMovement(Connection connection,
                                StockMovement movement)
            throws SQLException {

        InventoryItemDAO inventoryDAO = new InventoryItemDAO(connection);
        StockMovementDAO movementDAO = new StockMovementDAO(connection);

        if (!connection.getAutoCommit()) {
            throw new SQLException(
                    "The connection already has an active transaction.");
        }

        connection.setAutoCommit(false);

        try {
            InventoryItem item = inventoryDAO.findItem(
                    movement.getProductId(),
                    movement.getLocationId()
            );

            if (item == null) {
                throw new IllegalArgumentException(
                        "Assign the product to this location first.");
            }

            int newQuantity = calculator.calculateNewQuantity(
                    item.getQuantity(),
                    movement.getMovementType(),
                    movement.getQuantity()
            );

            boolean updated = inventoryDAO.updateQuantity(
                    item.getProductId(),
                    item.getLocationId(),
                    item.getQuantity(),
                    newQuantity
            );

            if (!updated) {
                throw new IllegalArgumentException(
                        "Stock changed during this operation. "
                                + "Refresh and try again.");
            }

            movementDAO.addMovement(movement);

            connection.commit();

        } catch (SQLException | RuntimeException exception) {
            try {
                connection.rollback();
            } catch (SQLException rollbackException) {
                exception.addSuppressed(rollbackException);
            }

            throw exception;
        }
    }
}