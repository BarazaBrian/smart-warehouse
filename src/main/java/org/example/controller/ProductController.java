package org.example.controller;

import org.example.model.Product;
import org.example.service.ProductService;
import org.example.ui.ProductPanel;

import javax.swing.*;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ProductController {

    private final ProductPanel panel;
    private final ProductService productService;

    public ProductController(ProductPanel panel,
                             ProductService productService) {
        this.panel = panel;
        this.productService = productService;
    }

    public void refreshProducts() {

        if (productService == null) {
            return;
        }

        panel.setRefreshEnabled(false);

        SwingWorker<List<Product>, Void> worker =
                new SwingWorker<List<Product>, Void>() {

                    @Override
                    protected List<Product> doInBackground() throws SQLException {
                        return productService.getAllProducts();
                    }

                    @Override
                    protected void done() {
                        try {
                            panel.displayProducts(get());

                        } catch (InterruptedException exception) {
                            Thread.currentThread().interrupt();
                            showError("Loading was interrupted. Please try again.");

                        } catch (ExecutionException exception) {
                            showError(
                                    "Could not load products. Check the database "
                                            + "connection and table setup."
                            );

                        } finally {
                            panel.setRefreshEnabled(true);
                        }
                    }
                };

        worker.execute();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                panel,
                message,
                "Product error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}