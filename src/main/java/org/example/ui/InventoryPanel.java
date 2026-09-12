package org.example.ui;

import org.example.service.InventoryValidator;
import org.example.service.InventoryService;
import org.example.service.StockMovementService;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

import org.example.model.InventoryItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.util.List;
import org.example.model.MovementType;
import java.awt.GridLayout;

import org.example.model.StockMovement;
import org.example.service.StockMovementValidator;
import java.time.LocalDateTime;


public class InventoryPanel extends JPanel {

    private final StockMovementService stockMovementService;

    private final InventoryService inventoryService;

    private final JTextField productIdField = new JTextField();
    private final JTextField locationIdField = new JTextField();
    private final JTextField quantityField = new JTextField();

    private final JComboBox<MovementType> movementTypeBox =
            new JComboBox<>(MovementType.values());

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Product ID", "Location ID", "Quantity"},
            0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable inventoryTable = new JTable(tableModel);

    public InventoryPanel() {
        this(null, null);
    }

    public InventoryPanel(StockMovementService stockMovementService,
                          InventoryService inventoryService) {
        this.stockMovementService = stockMovementService;
        this.inventoryService = inventoryService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));

        form.add(new JLabel("Product ID:"));
        form.add(productIdField);

        form.add(new JLabel("Shelf location ID:"));
        form.add(locationIdField);

        form.add(new JLabel("Movement type:"));
        form.add(movementTypeBox);

        form.add(new JLabel("Movement quantity:"));
        form.add(quantityField);

        add(form, BorderLayout.NORTH);

        inventoryTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );
        inventoryTable.setFillsViewportHeight(true);

        add(new JScrollPane(inventoryTable), BorderLayout.CENTER);

        JPanel buttons = new JPanel();

        JButton assignButton = new JButton("Assign to Shelf");
        JButton recordButton = new JButton("Record Movement");
        JButton refreshButton = new JButton("Refresh");
        JButton historyButton = new JButton("View History");
        JButton clearButton = new JButton("Clear");

        buttons.add(assignButton);
        buttons.add(recordButton);
        buttons.add(refreshButton);
        buttons.add(historyButton);
        buttons.add(clearButton);

        add(buttons, BorderLayout.SOUTH);

        clearButton.addActionListener(event -> {
            productIdField.setText("");
            locationIdField.setText("");
            quantityField.setText("");

            movementTypeBox.setSelectedItem(MovementType.IN);
            inventoryTable.clearSelection();
            productIdField.requestFocusInWindow();
        });

// TODO (Integration with Elera): Connect assignment only after
// checking that the product exists and the location is a shelf.        assignButton.setEnabled(false);
        recordButton.setEnabled(stockMovementService != null);
        recordButton.addActionListener(
                event -> recordMovement(recordButton, refreshButton)
        );
        refreshButton.setEnabled(inventoryService != null);
        refreshButton.addActionListener(event -> refreshInventory(refreshButton));
        historyButton.setEnabled(stockMovementService != null);
        historyButton.addActionListener(event -> viewHistory(historyButton));

        // TODO (Integration with Elera):
        // Connect InventoryService and display product/location names.
    }

    private void viewHistory(JButton historyButton) {

        if (stockMovementService == null) {
            return;
        }

        historyButton.setEnabled(false);

        SwingWorker<List<StockMovement>, Void> worker =
                new SwingWorker<List<StockMovement>, Void>() {

                    @Override
                    protected List<StockMovement> doInBackground()
                            throws SQLException {
                        return stockMovementService.getAllMovements();
                    }

                    @Override
                    protected void done() {
                        try {
                            displayHistory(get());

                        } catch (InterruptedException exception) {
                            Thread.currentThread().interrupt();

                            JOptionPane.showMessageDialog(
                                    InventoryPanel.this,
                                    "Loading history was interrupted.",
                                    "Loading interrupted",
                                    JOptionPane.ERROR_MESSAGE
                            );

                        } catch (ExecutionException exception) {
                            JOptionPane.showMessageDialog(
                                    InventoryPanel.this,
                                    "Could not load history. Check the database "
                                            + "connection and table setup.",
                                    "History error",
                                    JOptionPane.ERROR_MESSAGE
                            );

                        } finally {
                            historyButton.setEnabled(true);
                        }
                    }
                };

        worker.execute();
    }

    private void displayHistory(List<StockMovement> movements) {

        DefaultTableModel historyModel = new DefaultTableModel(
                new String[]{
                        "ID", "Product ID", "Location ID",
                        "Type", "Quantity", "Date and time"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (StockMovement movement : movements) {
            historyModel.addRow(new Object[]{
                    movement.getId(),
                    movement.getProductId(),
                    movement.getLocationId(),
                    movement.getMovementType(),
                    movement.getQuantity(),
                    movement.getDate()
            });
        }

        JTable historyTable = new JTable(historyModel);
        historyTable.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setPreferredSize(new java.awt.Dimension(800, 350));

        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "Stock Movement History",
                JOptionPane.PLAIN_MESSAGE
        );
    }

    private void refreshInventory(JButton refreshButton) {

        if (inventoryService == null) {
            return;
        }

        refreshButton.setEnabled(false);

        SwingWorker<List<InventoryItem>, Void> worker =
                new SwingWorker<List<InventoryItem>, Void>() {

                    @Override
                    protected List<InventoryItem> doInBackground()
                            throws SQLException {
                        return inventoryService.getAllItems();
                    }

                    @Override
                    protected void done() {
                        try {
                            displayItems(get());

                        } catch (InterruptedException exception) {
                            Thread.currentThread().interrupt();

                            JOptionPane.showMessageDialog(
                                    InventoryPanel.this,
                                    "Loading was interrupted. Please try again.",
                                    "Loading interrupted",
                                    JOptionPane.ERROR_MESSAGE
                            );

                        } catch (ExecutionException exception) {
                            JOptionPane.showMessageDialog(
                                    InventoryPanel.this,
                                    "Could not load inventory. Check the database "
                                            + "connection and table setup.",
                                    "Inventory error",
                                    JOptionPane.ERROR_MESSAGE
                            );

                        } finally {
                            refreshButton.setEnabled(true);
                        }
                    }
                };

        worker.execute();
    }

    private void recordMovement(JButton recordButton,
                                JButton refreshButton) {

        if (stockMovementService == null) {
            return;
        }

        StockMovement movement;

        try {
            movement = readMovementForm();
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getMessage(),
                    "Invalid input",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        recordButton.setEnabled(false);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws SQLException {
                stockMovementService.recordMovement(movement);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();

                    JOptionPane.showMessageDialog(
                            InventoryPanel.this,
                            "Stock movement recorded successfully."
                    );

                    refreshInventory(refreshButton);

                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();

                    JOptionPane.showMessageDialog(
                            InventoryPanel.this,
                            "Recording was interrupted. Check movement "
                                    + "history before trying again.",
                            "Recording interrupted",
                            JOptionPane.ERROR_MESSAGE
                    );

                } catch (ExecutionException exception) {
                    Throwable cause = exception.getCause();

                    String message = "Could not record the movement. "
                            + "Check the database connection and setup.";

                    if (cause instanceof IllegalArgumentException) {
                        message = cause.getMessage();
                    }

                    JOptionPane.showMessageDialog(
                            InventoryPanel.this,
                            message,
                            "Movement error",
                            JOptionPane.ERROR_MESSAGE
                    );

                } finally {
                    recordButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private InventoryItem readAssignmentForm() {

        int productId;
        int locationId;

        try {
            productId = Integer.parseInt(productIdField.getText().trim());
            locationId = Integer.parseInt(locationIdField.getText().trim());

        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Product ID and location ID must be whole numbers.");
        }

        InventoryItem item = new InventoryItem(productId, locationId, 0);

        InventoryValidator validator = new InventoryValidator();
        validator.validate(item);

        return item;
    }


    private StockMovement readMovementForm() {

        int productId;
        int locationId;
        int quantity;

        try {
            productId = Integer.parseInt(productIdField.getText().trim());
            locationId = Integer.parseInt(locationIdField.getText().trim());
            quantity = Integer.parseInt(quantityField.getText().trim());

        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Product ID, location ID and quantity must be whole numbers.");
        }

        MovementType type =
                (MovementType) movementTypeBox.getSelectedItem();

        StockMovement movement = new StockMovement(
                productId,
                locationId,
                type,
                quantity,
                LocalDateTime.now()
        );

        StockMovementValidator validator = new StockMovementValidator();
        validator.validate(movement);

        return movement;
    }

    public void displayItems(List<InventoryItem> items) {
        tableModel.setRowCount(0);

        for (InventoryItem item : items) {
            tableModel.addRow(new Object[]{
                    item.getProductId(),
                    item.getLocationId(),
                    item.getQuantity()
            });
        }
    }
}