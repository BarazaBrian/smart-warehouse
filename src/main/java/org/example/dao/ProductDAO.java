package org.example.dao;

import org.example.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    private final Connection connection;

    // TODO (Elera): Provide an open MySQL connection.
    // The code that opens the connection must also close it.
    public ProductDAO(Connection connection) {
        this.connection = connection;
    }

    public void addProduct(Product product) throws SQLException {

        // TODO (Elera): Create the products table automatically at startup.
        // Required columns:
        // id                  - INT, auto-increment primary key
        // name                - VARCHAR(100), not null
        // sku                 - VARCHAR(50), unique, not null
        // unit_price          - DECIMAL(12, 2), not null
        // minimum_stock_level - INT, not null
        // expiry_date         - DATE, allows NULL

        String sql = """
                INSERT INTO products
                    (name, sku, unit_price, minimum_stock_level, expiry_date)
                VALUES (?, ?, ?, ?, ?)
                """;

        // Closing the statement does not close the supplied connection.
        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, product.getName());
            statement.setString(2, product.getSku());
            statement.setBigDecimal(3, product.getUnitPrice());
            statement.setInt(4, product.getMinimumStockLevel());

            if (product.getExpiryDate() == null) {
                statement.setNull(5, Types.DATE);
            } else {
                statement.setDate(
                        5,
                        java.sql.Date.valueOf(product.getExpiryDate())
                );
            }

            statement.executeUpdate();
        }
    }

    public List<Product> getAllProducts() throws SQLException {

        List<Product> products = new ArrayList<>();

        String sql = """
            SELECT id, name, sku, unit_price,
                   minimum_stock_level, expiry_date
            FROM products
            ORDER BY name, id
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

            while (results.next()) {

                java.sql.Date expiryDate = results.getDate("expiry_date");

                Product product = new Product(
                        results.getInt("id"),
                        results.getString("name"),
                        results.getString("sku"),
                        results.getBigDecimal("unit_price"),
                        results.getInt("minimum_stock_level"),
                        expiryDate == null ? null : expiryDate.toLocalDate()
                );

                products.add(product);
            }
        }

        return products;
    }

    public boolean updateProduct(Product product) throws SQLException {

        String sql = """
            UPDATE products
            SET name = ?, sku = ?, unit_price = ?,
                minimum_stock_level = ?, expiry_date = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, product.getName());
            statement.setString(2, product.getSku());
            statement.setBigDecimal(3, product.getUnitPrice());
            statement.setInt(4, product.getMinimumStockLevel());

            if (product.getExpiryDate() == null) {
                statement.setNull(5, Types.DATE);
            } else {
                statement.setDate(
                        5,
                        java.sql.Date.valueOf(product.getExpiryDate())
                );
            }

            statement.setInt(6, product.getId());

            return statement.executeUpdate() > 0;
        }
    }
}