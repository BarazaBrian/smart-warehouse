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

    public void updateProduct() {

        if (productService == null || !panel.isRefreshEnabled()) {
            return;
        }

        Product product;

        try {
            product = panel.readSelectedProduct();
        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());
            return;
        }

        panel.setSaving(true);

        SwingWorker<Boolean, Void> worker =
                new SwingWorker<Boolean, Void>() {

                    @Override
                    protected Boolean doInBackground() throws SQLException {
                        return productService.updateProduct(product);
                    }

                    @Override
                    protected void done() {
                        boolean updated = false;

                        try {
                            updated = get();

                            if (updated) {
                                JOptionPane.showMessageDialog(
                                        panel,
                                        "Product updated successfully."
                                );
                            } else {
                                showError(
                                        "No update was reported. Refresh the table "
                                                + "and check the product."
                                );
                            }

                        } catch (InterruptedException exception) {
                            Thread.currentThread().interrupt();

                            showError(
                                    "Updating was interrupted. Refresh to check "
                                            + "the saved details."
                            );

                        } catch (ExecutionException exception) {
                            Throwable cause = exception.getCause();

                            if (cause instanceof IllegalArgumentException) {
                                showError(cause.getMessage());
                            } else {
                                showError(
                                        "Could not update the product. Check the "
                                                + "database connection and ensure "
                                                + "the SKU is unique."
                                );
                            }

                        } finally {
                            panel.setSaving(false);
                        }

                        if (updated) {
                            refreshProducts();
                        }
                    }
                };

        worker.execute();
    }

    public void deleteProduct() {

        if (productService == null || !panel.isRefreshEnabled()) {
            return;
        }

        int id;

        try {
            id = panel.getSelectedProductId();
        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());
            return;
        }

        int answer = JOptionPane.showConfirmDialog(
                panel,
                "Delete product ID " + id + "? This cannot be undone.",
                "Confirm deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        panel.setSaving(true);

        SwingWorker<Boolean, Void> worker =
                new SwingWorker<Boolean, Void>() {

                    @Override
                    protected Boolean doInBackground() throws SQLException {
                        return productService.deleteProduct(id);
                    }

                    @Override
                    protected void done() {
                        boolean deleted = false;

                        try {
                            deleted = get();

                            if (deleted) {
                                JOptionPane.showMessageDialog(
                                        panel,
                                        "Product deleted successfully."
                                );
                            } else {
                                showError(
                                        "Product was not found. Refresh the table."
                                );
                            }

                        } catch (InterruptedException exception) {
                            Thread.currentThread().interrupt();

                            showError(
                                    "Deletion was interrupted. Refresh to check "
                                            + "whether the product was deleted."
                            );

                        } catch (ExecutionException exception) {
                            showError(
                                    "Could not delete the product. It may have "
                                            + "linked inventory or stock movements, "
                                            + "or the database may be unavailable."
                            );

                        } finally {
                            panel.setSaving(false);
                        }

                        if (deleted) {
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