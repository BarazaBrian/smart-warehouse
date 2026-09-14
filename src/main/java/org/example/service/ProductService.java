package org.example.service;
import org.example.dao.InventoryItemDAO;

import java.time.LocalDate;
import java.util.ArrayList;

import org.example.dao.ProductDAO;
import org.example.model.Product;

import java.sql.SQLException;
import java.util.List;

public class ProductService {

    private final ProductDAO productDAO;
    private final InventoryItemDAO inventoryDAO;
    private final ProductValidator validator;

    public ProductService(ProductDAO productDAO) {
        this(productDAO, null);
    }

    // TODO (Integration with Elera):
// Supply both DAOs to enable low-stock queries.
// Both should use the same open connection for this service.
    public ProductService(ProductDAO productDAO,
                          InventoryItemDAO inventoryDAO) {
        this.productDAO = productDAO;
        this.inventoryDAO = inventoryDAO;
        this.validator = new ProductValidator();
    }

    public void addProduct(Product product) throws SQLException {
        validator.validate(product);
        productDAO.addProduct(product);
    }

    public List<Product> getAllProducts() throws SQLException {
        return productDAO.getAllProducts();
    }

    public boolean updateProduct(Product product) throws SQLException {
        validator.validate(product);
        validateId(product.getId());

        return productDAO.updateProduct(product);
    }

    public boolean deleteProduct(int id) throws SQLException {
        validateId(id);

        return productDAO.deleteProduct(id);
    }

    public List<Product> getNearExpiryProducts(LocalDate today)
            throws SQLException {

        List<Product> nearExpiryProducts = new ArrayList<>();

        for (Product product : productDAO.getAllProducts()) {
            if (product.isNearExpiry(today)) {
                nearExpiryProducts.add(product);
            }
        }

        return nearExpiryProducts;
    }

    public List<Product> getLowStockProducts() throws SQLException {

        if (inventoryDAO == null) {
            throw new SQLException(
                    "Inventory access has not been configured.");
        }

        List<Product> lowStockProducts = new ArrayList<>();

        for (Product product : productDAO.getAllProducts()) {

            long totalQuantity =
                    inventoryDAO.getTotalQuantity(product.getId());

            if (product.isLowStock(totalQuantity)) {
                lowStockProducts.add(product);
            }
        }

        return lowStockProducts;
    }

    private void validateId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Select an existing product first.");
        }
    }
}
