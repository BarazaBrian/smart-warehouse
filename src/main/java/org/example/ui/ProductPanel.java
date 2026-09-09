package org.example.ui;


import java.util.List;

import org.example.service.ProductService;
import org.example.model.Product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;

import org.example.controller.ProductController;


public class ProductPanel extends JPanel {

    private final JTextField nameField = new JTextField();
    private final JTextField skuField = new JTextField();
    private final JTextField priceField = new JTextField();
    private final JTextField minimumStockField = new JTextField();
    private final JTextField expiryField = new JTextField();
    private final ProductService productService;
    private final ProductController controller;

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
    private final JButton addButton = new JButton("Add");
    private final JButton updateButton = new JButton("Update");
    private final JButton deleteButton = new JButton("Delete");
    private final JButton refreshButton = new JButton("Refresh");
    private final JButton clearButton = new JButton("Clear");

    public ProductPanel() {
        this(null);
    }

    public ProductPanel(ProductService productService) {
        this.productService = productService;
        this.controller = new ProductController(this, productService);

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

        productTable.getSelectionModel().addListSelectionListener(event -> {

            if (event.getValueIsAdjusting()) {
                return;
            }

            int selectedRow = productTable.getSelectedRow();

            if (selectedRow == -1) {
                return;
            }

            int modelRow = productTable.convertRowIndexToModel(selectedRow);

            nameField.setText(tableModel.getValueAt(modelRow, 1).toString());
            skuField.setText(tableModel.getValueAt(modelRow, 2).toString());
            priceField.setText(tableModel.getValueAt(modelRow, 3).toString());
            minimumStockField.setText(
                    tableModel.getValueAt(modelRow, 4).toString()
            );

            Object expiryDate = tableModel.getValueAt(modelRow, 5);

            expiryField.setText(
                    expiryDate == null ? "" : expiryDate.toString()
            );
        });

        JScrollPane tableScrollPane = new JScrollPane(productTable);
        add(tableScrollPane, BorderLayout.CENTER);

        JPanel buttons = new JPanel();


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

        addButton.setEnabled(productService != null);
        addButton.addActionListener(event -> controller.addProduct());

// TODO (Brian and Elera): Verify all CRUD actions against MySQL,
// including rejection of deleting products with linked records.
        updateButton.setEnabled(productService != null);
        updateButton.addActionListener(event -> controller.updateProduct());
        deleteButton.setEnabled(productService != null);
        deleteButton.addActionListener(event -> controller.deleteProduct());

        refreshButton.setEnabled(productService != null);
        refreshButton.addActionListener(event -> loadProducts(refreshButton));
        // TODO (Integration with Elera):
        // Connect ProductService after database setup is available.
    }

    public boolean isRefreshEnabled() {
        return refreshButton.isEnabled();
    }

    public void setSaving(boolean saving) {
        boolean enabled = !saving && productService != null;

        addButton.setEnabled(enabled);
        updateButton.setEnabled(enabled);
        deleteButton.setEnabled(enabled);
        refreshButton.setEnabled(enabled);
    }

    public int getSelectedProductId() {

        int selectedRow = productTable.getSelectedRow();

        if (selectedRow == -1) {
            throw new IllegalArgumentException(
                    "Select a product from the table first.");
        }

        int modelRow = productTable.convertRowIndexToModel(selectedRow);

        return ((Number) tableModel.getValueAt(modelRow, 0)).intValue();
    }

    public Product readSelectedProduct() {

        int selectedRow = productTable.getSelectedRow();

        if (selectedRow == -1) {
            throw new IllegalArgumentException(
                    "Select a product from the table first.");
        }

        int modelRow = productTable.convertRowIndexToModel(selectedRow);
        int id = ((Number) tableModel.getValueAt(modelRow, 0)).intValue();

        Product editedProduct = readProductForm();

        return new Product(
                id,
                editedProduct.getName(),
                editedProduct.getSku(),
                editedProduct.getUnitPrice(),
                editedProduct.getMinimumStockLevel(),
                editedProduct.getExpiryDate()
        );
    }

    public Product readProductForm() {

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
        controller.refreshProducts();
    }

    public void setRefreshEnabled(boolean enabled) {
        boolean available = enabled && productService != null;

        refreshButton.setEnabled(available);
        addButton.setEnabled(available);
        updateButton.setEnabled(available);
        deleteButton.setEnabled(available);
    }

    public void displayProducts(List<Product> products) {
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
    }
}