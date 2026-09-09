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

    public void addProduct() {

        if (productService == null || !panel.isRefreshEnabled()) {
            return;
        }

        Product product;

        try {
            product = panel.readProductForm();
        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());
            return;
        }

        panel.setSaving(true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws SQLException {
                productService.addProduct(product);
                return null;
            }

            @Override
            protected void done() {
                boolean saved = false;

                try {
                    get();
                    saved = true;

                    JOptionPane.showMessageDialog(
                            panel,
                            "Product added successfully."
                    );

                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();

                    showError(
                            "Saving was interrupted. Refresh to check "
                                    + "whether the product was saved."
                    );

                } catch (ExecutionException exception) {
                    Throwable cause = exception.getCause();

                    if (cause instanceof IllegalArgumentException) {
                        showError(cause.getMessage());
                    } else {
                        showError(
                                "Could not save the product. Check the "
                                        + "database connection and ensure "
                                        + "the SKU is unique."
                        );
                    }

                } finally {
                    panel.setSaving(false);
                }

                if (saved) {
                    refreshProducts();
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