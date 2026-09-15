package org.example;

import org.example.ui.ProductPanel;
import org.example.ui.InventoryPanel;
import org.example.ui.StorageLocationPanel;

import org.example.dao.ProductDAO;
import org.example.dao.InventoryItemDAO;
import org.example.service.ProductService;
import org.example.service.InventoryService;
import org.example.service.StockMovementService;
import org.example.util.DBConnection;
import org.example.util.SchemaInitializer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.JTabbedPane;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createWindow);
    }

    private static void createWindow() {

        JFrame window = new JFrame("Smart Warehouse");

        JLabel heading = new JLabel(
                "Smart Warehouse — Inventory Manager",
                SwingConstants.CENTER
        );

        window.add(heading, BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();

        // Connect to MySQL and create every table this part of the
        // app knows about. If this fails, there's no point opening
        // any screen, so show the real error and stop here.
        DataSource dataSource;
        Connection productConnection;

        try {
            dataSource = DBConnection.getDataSource();
            productConnection = DBConnection.getConnection();

            SchemaInitializer.initAll(productConnection);
            // stock_movements is not created here yet, its exact
            // columns still need confirming against StockMovementDAO.

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Could not connect to the database: " + e.getMessage());
            return;
        }

        // ProductService needs DAOs built on ONE shared, long-lived
        // connection, kept open for as long as the app runs.
        ProductDAO productDAO = new ProductDAO(productConnection);
        InventoryItemDAO inventoryDAO = new InventoryItemDAO(productConnection);
        ProductService productService = new ProductService(productDAO, inventoryDAO);

        // InventoryService and StockMovementService instead take the
        // DataSource itself, and open a fresh connection per call.
        InventoryService inventoryService = new InventoryService(dataSource);
        StockMovementService stockMovementService = new StockMovementService(dataSource);

        tabs.addTab("Products", new ProductPanel(productService));

        // Order matters here: StockMovementService first, then
        // InventoryService, matches InventoryPanel's real constructor.
        tabs.addTab("Inventory", new InventoryPanel(stockMovementService, inventoryService));

        tabs.addTab("Storage Locations", new StorageLocationPanel());

        window.add(tabs, BorderLayout.CENTER);
        window.setSize(1000, 650);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLocationRelativeTo(null);

        // Close the shared connection when the window closes, per
        // Brian's original TODO.
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    productConnection.close();
                } catch (SQLException ex) {
                    // Closing on shutdown, nothing useful to do if this fails.
                }
            }
        });

        window.setVisible(true);
    }
}