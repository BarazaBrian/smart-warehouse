package org.example.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates the storage_location table on application startup, rather
 * than requiring it to be created by hand, per the spec requirement
 * that the schema be created by the application on first run.
 *
 * Only storage_location is here, since it's Elera's part. If Brian
 * has (or writes) an equivalent for product/inventory_item/
 * stock_movement, call both from the same startup point in Main, don't
 * fork schema setup into two separate, uncoordinated entry points.
 */
public class SchemaInitializer {

    public static void initStorageLocationTable(Connection conn) throws SQLException {
        String sql =
                "CREATE TABLE IF NOT EXISTS storage_location (" +
                        "location_id INT PRIMARY KEY AUTO_INCREMENT, " +
                        "name VARCHAR(100) NOT NULL, " +
                        "type VARCHAR(20) NOT NULL CHECK (type IN ('WAREHOUSE','ZONE','SHELF')), " +
                        "parent_id INT NULL, " +
                        "FOREIGN KEY (parent_id) REFERENCES storage_location(location_id))";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    /**
     * Columns match exactly what ProductDAO.addProduct()'s TODO comment
     * asks for.
     */
    public static void initProductsTable(Connection conn) throws SQLException {
        String sql =
                "CREATE TABLE IF NOT EXISTS products (" +
                        "id INT PRIMARY KEY AUTO_INCREMENT, " +
                        "name VARCHAR(100) NOT NULL, " +
                        "sku VARCHAR(50) NOT NULL UNIQUE, " +
                        "unit_price DECIMAL(12,2) NOT NULL, " +
                        "minimum_stock_level INT NOT NULL, " +
                        "expiry_date DATE NULL)";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    /**
     * Columns and constraints match exactly what InventoryItemDAO's TODO
     * comment asks for: composite primary key, ON DELETE RESTRICT on
     * both foreign keys so a product or a location can't be deleted
     * while inventory still references it.
     *
     * Must run AFTER initProductsTable() and initStorageLocationTable(),
     * since this table has foreign keys pointing at both of them.
     */
    public static void initInventoryItemsTable(Connection conn) throws SQLException {
        String sql =
                "CREATE TABLE IF NOT EXISTS inventory_items (" +
                        "product_id INT NOT NULL, " +
                        "location_id INT NOT NULL, " +
                        "quantity INT NOT NULL CHECK (quantity >= 0), " +
                        "PRIMARY KEY (product_id, location_id), " +
                        "FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT, " +
                        "FOREIGN KEY (location_id) REFERENCES storage_location(location_id) ON DELETE RESTRICT)";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    /**
     * Columns and constraints match exactly what StockMovementDAO's TODO
     * comment asks for: a composite foreign key pointing at
     * inventory_items, not at products or storage_location directly,
     * since a movement only makes sense for a product already assigned
     * to that specific location.
     *
     * Must run AFTER initInventoryItemsTable(), since this table's
     * foreign key points at that one.
     */
    public static void initStockMovementsTable(Connection conn) throws SQLException {
        String sql =
                "CREATE TABLE IF NOT EXISTS stock_movements (" +
                        "id INT PRIMARY KEY AUTO_INCREMENT, " +
                        "product_id INT NOT NULL, " +
                        "location_id INT NOT NULL, " +
                        "movement_type VARCHAR(3) NOT NULL CHECK (movement_type IN ('IN','OUT')), " +
                        "quantity INT NOT NULL CHECK (quantity > 0), " +
                        "movement_date DATETIME NOT NULL, " +
                        "FOREIGN KEY (product_id, location_id) REFERENCES inventory_items(product_id, location_id) ON DELETE RESTRICT)";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    /**
     * Creates every table, in the order their foreign keys require:
     * storage_location and products have none, inventory_items depends
     * on both of those, stock_movements depends on inventory_items.
     */
    public static void initAll(Connection conn) throws SQLException {
        initStorageLocationTable(conn);
        initProductsTable(conn);
        initInventoryItemsTable(conn);
        initStockMovementsTable(conn);
    }
}