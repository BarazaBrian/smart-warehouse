package org.example.ui;

import org.example.model.InventoryItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.util.List;
import org.example.model.MovementType;
import java.awt.GridLayout;

public class InventoryPanel extends JPanel {

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

// TODO (Brian): Connect these buttons to inventory and movement services.
        assignButton.setEnabled(false);
        recordButton.setEnabled(false);
        refreshButton.setEnabled(false);
        historyButton.setEnabled(false);

        // TODO (Integration with Elera):
        // Connect InventoryService and display product/location names.
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