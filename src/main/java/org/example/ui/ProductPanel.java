package org.example.ui;

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

        // TODO (Brian): Add buttons, load product rows and handle selection.

        // TODO (Integration with Elera):
        // Connect ProductService after database setup is available.
    }
}