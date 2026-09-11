package org.example.service;

import org.example.dao.InventoryItemDAO;
import org.example.model.InventoryItem;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class InventoryService {

    private final DataSource dataSource;

    // TODO (Elera): Supply a configured MySQL DataSource.
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

    // TODO (Integration with Elera):
    // Before assigning products, verify that the location is a shelf.
}