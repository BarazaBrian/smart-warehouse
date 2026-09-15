package org.example.service;

import java.util.List;

import org.example.dao.InventoryItemDAO;
import org.example.dao.StockMovementDAO;
import org.example.dao.StorageLocationDAO;
import org.example.model.InventoryItem;
import org.example.model.MovementType;
import org.example.model.StockMovement;
import org.example.model.StorageLocation;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;


public class StockMovementService {

    private final DataSource dataSource;

    private final StockMovementValidator validator =
            new StockMovementValidator();

    private final StockCalculator calculator = new StockCalculator();

    public StockMovementService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<StockMovement> getAllMovements() throws SQLException {

        try (Connection connection = dataSource.getConnection()) {

            StockMovementDAO movementDAO = new StockMovementDAO(connection);

            return movementDAO.getAllMovements();
        }
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

                if (movement.getMovementType() == MovementType.OUT) {
                    throw new IllegalArgumentException(
                            "No stock exists for this product at this location.");
                }

                // First-ever IN movement for this product/location pair.
                // Verify the location is a SHELF before creating the
                // inventory record, this is the same rule InventoryService
                // uses for a manual assignment, just triggered here
                // automatically instead of through a separate button.
                StorageLocation location =
                        new StorageLocationDAO().findById(movement.getLocationId());

                if (location == null) {
                    throw new IllegalArgumentException(
                            "No storage location found with that id.");
                }

                if (!HierarchyRules.isValidInventoryLocation(location.getType())) {
                    throw new IllegalArgumentException(
                            "Products can only be assigned to a shelf, not a "
                                    + location.getType() + ".");
                }

                // Uses the SAME connection/inventoryDAO as the rest of
                // this method, so this insert is part of the same
                // transaction as the quantity update and movement below,
                // if anything fails after this, it rolls back too.
                inventoryDAO.assignProductToLocation(
                        movement.getProductId(),
                        movement.getLocationId()
                );

                item = inventoryDAO.findItem(
                        movement.getProductId(),
                        movement.getLocationId()
                );
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