package org.example.service;

import org.example.dao.InventoryItemDAO;
import org.example.model.InventoryItem;

import java.sql.SQLException;
import java.util.List;

public class InventoryService {

    private final InventoryItemDAO inventoryDAO;

    public InventoryService(InventoryItemDAO inventoryDAO) {
        this.inventoryDAO = inventoryDAO;
    }

    public List<InventoryItem> getAllItems() throws SQLException {
        return inventoryDAO.getAllItems();
    }

    // TODO (Integration with Elera):
    // Before implementing product assignment, connect the location
    // lookup so we can verify that the selected location is a shelf.
}