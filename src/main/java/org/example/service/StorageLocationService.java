package org.example.service;

import org.example.dao.StorageLocationDAO;
import org.example.model.StorageLocation;
import org.example.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads the whole subtree beneath a location once via the DAO, then
 * recurses over the in-memory objects instead of querying the
 * database at every recursive step. That's the difference between
 * one database round trip and dozens: build the tree first, then
 * let StorageLocation.calculateTotalStockValue() walk it in memory.
 */
public class StorageLocationService {

    private final StorageLocationDAO dao;

    public StorageLocationService(StorageLocationDAO dao) {
        this.dao = dao;
    }

    /**
     * Calculates the total stock value under a location, using
     * whatever values are supplied in stockValueByLocationId. Useful
     * for testing with made-up numbers without touching the database.
     * For the real app, use the other calculateTotalStockValue(int)
     * below instead, which reads genuine data.
     */
    public BigDecimal calculateTotalStockValue(int rootId, Map<Integer, BigDecimal> stockValueByLocationId) throws SQLException {
        StorageLocation root = buildTree(rootId, stockValueByLocationId);
        if (root == null) {
            throw new IllegalArgumentException("No location found for id " + rootId);
        }
        return root.calculateTotalStockValue();
    }

    /**
     * The version the actual app uses: reads real stock values from
     * the database (quantity * unit_price for every product currently
     * assigned to each location), then runs the same calculation.
     */
    public BigDecimal calculateTotalStockValue(int rootId) throws SQLException {
        Map<Integer, BigDecimal> stockValueByLocationId = loadStockValuesFromDatabase();
        return calculateTotalStockValue(rootId, stockValueByLocationId);
    }

    /**
     * Runs one query that joins inventory_items to products, so we get
     * each location's total stock value (quantity times price, summed
     * per location) in a single trip to the database, rather than
     * looking up prices one product at a time in Java code.
     *
     * GROUP BY ii.location_id means: instead of one row per
     * product-at-a-location, collapse all the rows for the same
     * location into one row, with SUM(...) adding their values
     * together. The result is a small table: one row per location
     * that actually holds stock, with its total value.
     */
    private Map<Integer, BigDecimal> loadStockValuesFromDatabase() throws SQLException {
        Map<Integer, BigDecimal> values = new HashMap<>();

        String sql =
                "SELECT ii.location_id, SUM(ii.quantity * p.unit_price) AS total " +
                        "FROM inventory_items ii " +
                        "JOIN products p ON ii.product_id = p.id " +
                        "GROUP BY ii.location_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                values.put(rs.getInt("location_id"), rs.getBigDecimal("total"));
            }
        }

        return values;
    }

    /**
     * Builds an in-memory StorageLocation tree, starting at locationId
     * and recursively pulling in every descendant, before any total is
     * calculated. This is recursion too, just a different job than
     * StorageLocation.calculateTotalStockValue(): that method sums
     * values across a tree that already exists, this method builds
     * the tree itself, one level at a time, by calling itself on each
     * child returned by dao.findChildren().
     */
    private StorageLocation buildTree(int locationId, Map<Integer, BigDecimal> stockValueByLocationId) throws SQLException {
        StorageLocation node = dao.findById(locationId);
        if (node == null) {
            return null;
        }

        // Give this node its own direct stock value (zero if nothing
        // is stored directly here), then attach every child, each
        // built the same way, recursively.
        node.setDirectStockValue(stockValueByLocationId.getOrDefault(locationId, BigDecimal.ZERO));

        for (StorageLocation childStub : dao.findChildren(locationId)) {
            StorageLocation childTree = buildTree(childStub.getId(), stockValueByLocationId);
            node.addChild(childTree);
        }

        return node;
    }
}