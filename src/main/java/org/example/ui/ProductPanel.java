package org.example.ui;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.example.service.ProductService;
import org.example.model.Product;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;

public class ProductPanel extends JPanel {

    private final JTextField nameField = new JTextField();
    private final JTextField skuField = new JTextField();
    private final JTextField priceField = new JTextField();
    private final JTextField minimumStockField = new JTextField();
    private final JTextField expiryField = new JTextField();
    private final ProductService productService;

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{
                    "ID", "Name", "SKU", "Unit price (MUR)",
                    "Minimum stock", "Expiry date"
            },
            0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable productTable = new JTable(tableModel);

    public ProductPanel() {
        this(null);
    }

    public ProductPanel(ProductService productService) {
        this.productService = productService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 10));

        form.add(new JLabel("Product name:"));
        form.add(nameField);

        form.add(new JLabel("SKU:"));
        form.add(skuField);

        form.add(new JLabel("Unit price (MUR):"));
        form.add(priceField);

        form.add(new JLabel("Minimum stock level:"));
        form.add(minimumStockField);

        form.add(new JLabel("Expiry date (YYYY-MM-DD, optional):"));
        form.add(expiryField);

        add(form, BorderLayout.NORTH);

        productTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );
        productTable.setFillsViewportHeight(true);

        JScrollPane tableScrollPane = new JScrollPane(productTable);
        add(tableScrollPane, BorderLayout.CENTER);

        JPanel buttons = new JPanel();

        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton refreshButton = new JButton("Refresh");
        JButton clearButton = new JButton("Clear");

        buttons.add(addButton);
        buttons.add(updateButton);
        buttons.add(deleteButton);
        buttons.add(refreshButton);
        buttons.add(clearButton);

        add(buttons, BorderLayout.SOUTH);

        clearButton.addActionListener(event -> {
            nameField.setText("");
            skuField.setText("");
            priceField.setText("");
            minimumStockField.setText("");
            expiryField.setText("");

            productTable.clearSelection();
            nameField.requestFocusInWindow();
        });

// TODO (Brian): Enable these buttons when their handlers are connected.
        addButton.setEnabled(false);
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
        refreshButton.setEnabled(productService != null);

        refreshButton.addActionListener(event -> loadProducts(refreshButton));

// TODO (Brian): Connect button handlers, load products and handle selection.
        // TODO (Integration with Elera):
        // Connect ProductService after database setup is available.
    }
    private Product readProductForm() {

        String name = nameField.getText().trim();
        String sku = skuField.getText().trim();

        BigDecimal price;

        try {
            price = new BigDecimal(priceField.getText().trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Enter a valid unit price, for example 25.00.");
        }

        int minimumStock;

        try {
            minimumStock = Integer.parseInt(
                    minimumStockField.getText().trim()
            );
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Minimum stock must be a whole number, for example 10.");
        }

        LocalDate expiryDate = null;
        String expiryText = expiryField.getText().trim();

        if (!expiryText.isEmpty()) {
            try {
                expiryDate = LocalDate.parse(expiryText);
            } catch (DateTimeParseException exception) {
                throw new IllegalArgumentException(
                        "Enter a valid expiry date in YYYY-MM-DD format.");
            }
        }

        return new Product(name, sku, price, minimumStock, expiryDate);
    }

    private void loadProducts(JButton refreshButton) {

        if (productService == null) {
            return;
        }

        refreshButton.setEnabled(false);

        SwingWorker<List<Product>, Void> worker =
                new SwingWorker<List<Product>, Void>() {

                    @Override
                    protected List<Product> doInBackground() throws SQLException {
                        return productService.getAllProducts();
                    }

                    @Override
                    protected void done() {
                        try {
                            List<Product> products = get();

                            tableModel.setRowCount(0);

                            for (Product product : products) {
                                tableModel.addRow(new Object[]{
                                        product.getId(),
                                        product.getName(),
                                        product.getSku(),
                                        product.getUnitPrice(),
                                        product.getMinimumStockLevel(),
                                        product.getExpiryDate()
                                });
                            }

                        } catch (InterruptedException exception) {
                            Thread.currentThread().interrupt();

                            JOptionPane.showMessageDialog(
                                    ProductPanel.this,
                                    "Loading was interrupted. Please try again.",
                                    "Loading interrupted",
                                    JOptionPane.ERROR_MESSAGE
                            );

                        } catch (ExecutionException exception) {
                            JOptionPane.showMessageDialog(
                                    ProductPanel.this,
                                    "Could not load products. Check the database "
                                            + "connection and table setup, then try again.",
                                    "Database error",
                                    JOptionPane.ERROR_MESSAGE
                            );

                        } finally {
                            refreshButton.setEnabled(true);
                        }
                    }
                };

        worker.execute();
    }
}