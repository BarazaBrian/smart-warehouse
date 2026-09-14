package org.example;

import org.example.ui.ProductPanel;
import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.JTabbedPane;
import org.example.ui.InventoryPanel;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createWindow());
    }

    private static void createWindow() {

        JFrame window = new JFrame("Smart Warehouse");

        JLabel heading = new JLabel(
                "Smart Warehouse — Inventory Manager",
                SwingConstants.CENTER
        );

        window.add(heading, BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Products", new ProductPanel());
        tabs.addTab("Inventory", new InventoryPanel());

        window.add(tabs, BorderLayout.CENTER);
        window.setSize(1000, 650);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLocationRelativeTo(null);

        // TODO (Integration with Elera):
// Configure MySQL and initialise the schema before opening the screens.
// Pass a ProductService with both DAOs into ProductPanel.
// Pass StockMovementService and InventoryService into InventoryPanel.
// Add Elera's storage-location screen as another tab.
// Keep the product connection open while in use; close it on shutdown.

        window.setVisible(true);
    }
}