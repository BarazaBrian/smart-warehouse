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

                // A movement that isn't IN can't be the first-ever
                // record for this pair, there's nothing to take OUT of
                // a location that's never held this product.
                if (movement.getMovementType() == MovementType.OUT) {
                    throw new IllegalArgumentException(
                            "No stock exists for this product at this location.");
                }

                // First-ever IN movement for this product/location pair.
                // Look up the location and check its type before
                // creating anything, this is the same shelf-only rule
                // InventoryService used to enforce for a manual
                // assignment, applied here automatically instead.
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

                // inventoryDAO here is the SAME object used for the
                // quantity update and movement insert below, all three
                // built from the one connection this method opened.
                // That's what makes this insert part of the same
                // transaction, if anything later in this method fails,
                // this insert gets rolled back with it, not left behind.
                inventoryDAO.assignProductToLocation(
                        movement.getProductId(),
                        movement.getLocationId()
                );

                // Re-fetch the row we just created, so "item" below
                // has a real quantity (0) to calculate the new
                // quantity from, instead of staying null.
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