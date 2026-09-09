package org.example;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

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

        window.add(heading);
        window.setSize(1000, 650);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLocationRelativeTo(null);

        // TODO (Integration with Elera):
        // Initialise the database and connect the application services.
        // Add the product and storage-location screens with navigation.

        window.setVisible(true);
    }
}