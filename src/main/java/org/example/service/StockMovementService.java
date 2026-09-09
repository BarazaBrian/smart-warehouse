package org.example.service;

import org.example.dao.InventoryItemDAO;
import org.example.dao.StockMovementDAO;
import org.example.model.InventoryItem;
import org.example.model.StockMovement;

import java.sql.Connection;
import java.sql.SQLException;

public class StockMovementService {

    private final Connection connection;
    private final InventoryItemDAO inventoryDAO;
    private final StockMovementDAO movementDAO;

    private final StockMovementValidator validator =
            new StockMovementValidator();

    private final StockCalculator calculator = new StockCalculator();

    // TODO (Integration with Elera):
    // Supply an open connection dedicated to this operation.
    // The caller must close it after the operation finishes.
    public StockMovementService(Connection connection) {
        this.connection = connection;
        this.inventoryDAO = new InventoryItemDAO(connection);
        this.movementDAO = new StockMovementDAO(connection);
    }

    public void recordMovement(StockMovement movement)
            throws SQLException {

        validator.validate(movement);

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