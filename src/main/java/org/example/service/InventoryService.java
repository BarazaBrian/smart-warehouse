package org.example.service;

import org.example.dao.InventoryItemDAO;
import org.example.dao.StorageLocationDAO;
import org.example.model.InventoryItem;
import org.example.model.StorageLocation;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class InventoryService {

    private final DataSource dataSource;

    public InventoryService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<InventoryItem> getAllItems() throws SQLException {

        try (Connection connection = dataSource.getConnection()) {

            InventoryItemDAO inventoryDAO =
                    new InventoryItemDAO(connection);

            return inventoryDAO.getAllItems();
        }
    }

    /**
     * Assigns a product to a location, but only if that location is a
     * SHELF. A warehouse or a zone should never directly hold inventory.
     *
     * StorageLocationDAO manages its own connection internally, so it
     * doesn't need the DataSource here, this lookup is a separate,
     * independent read, not part of the same transaction as the insert
     * below.
     */
    public void assignProductToLocation(int productId, int locationId) throws SQLException {

        StorageLocation location = new StorageLocationDAO().findById(locationId);

        if (location == null) {
            throw new IllegalArgumentException("No storage location found with that id.");
        }

        if (!HierarchyRules.isValidInventoryLocation(location.getType())) {
            throw new IllegalArgumentException(
                    "Products can only be assigned to a shelf, not a " + location.getType() + ".");
        }

        try (Connection connection = dataSource.getConnection()) {
            InventoryItemDAO inventoryDAO = new InventoryItemDAO(connection);
            inventoryDAO.assignProductToLocation(productId, locationId);
        }
    }
}