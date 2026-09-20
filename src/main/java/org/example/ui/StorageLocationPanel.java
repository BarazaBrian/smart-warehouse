package org.example.ui;

import org.example.dao.StorageLocationDAO;
import org.example.model.LocationType;
import org.example.model.StorageLocation;
import org.example.service.StorageLocationService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Screen for creating and viewing the storage-location hierarchy
 * (warehouses, zones, shelves), and for running the recursive total
 * stock value calculation on a selected location.
 *
 * Follows the same layout pattern as Brian's ProductPanel: a form at
 * the top for entering a new row, a table in the middle showing all
 * existing rows, and a row of buttons at the bottom that act on
 * whatever's typed in the form or selected in the table.
 */
public class StorageLocationPanel extends JPanel {

    // The three input fields for adding a new location.
    private final JTextField nameField = new JTextField();
    private final JComboBox<LocationType> typeBox = new JComboBox<>(LocationType.values());
    private final JTextField parentIdField = new JTextField();

    // dao talks to the database directly. service sits on top of dao
    // and adds the recursive calculation. Both are created once, here,
    // and reused for the whole life of this screen.
    private final StorageLocationDAO dao = new StorageLocationDAO();
    private final StorageLocationService service = new StorageLocationService(dao);

    // tableModel holds the data actually shown in the table below.
    // Overriding isCellEditable(...) to always return false stops the
    // user from double-clicking a cell and typing over it directly,
    // all edits should go through the form and the buttons instead.
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Type", "Parent ID"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable locationTable = new JTable(tableModel);
    private final JButton addButton = new JButton("Add");
    private final JButton deleteButton = new JButton("Delete");
    private final JButton refreshButton = new JButton("Refresh");
    private final JButton clearButton = new JButton("Clear");
    private final JButton totalValueButton = new JButton("Calculate Total Value");

    public StorageLocationPanel() {

        // BorderLayout splits the screen into NORTH (top), CENTER
        // (middle), SOUTH (bottom), etc. We only use three of those
        // regions here.
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Build the form: a 3-row, 2-column grid, label next to field.
        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));

        form.add(new JLabel("Name:"));
        form.add(nameField);

        form.add(new JLabel("Type:"));
        form.add(typeBox);

        form.add(new JLabel("Parent ID (leave blank for a WAREHOUSE):"));
        form.add(parentIdField);

        add(form, BorderLayout.NORTH);

        // Only one row can be selected in the table at a time, since
        // Delete and Calculate Total Value only make sense for one
        // location at once.
        locationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        locationTable.setFillsViewportHeight(true);
        add(new JScrollPane(locationTable), BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        buttons.add(addButton);
        buttons.add(deleteButton);
        buttons.add(refreshButton);
        buttons.add(clearButton);
        buttons.add(totalValueButton);
        add(buttons, BorderLayout.SOUTH);

        // Each button's addActionListener says "when this button is
        // clicked, run this method." The event -> methodName() syntax
        // is a lambda, a short way of writing "do this when triggered"
        // without a whole separate named class for each button.
        addButton.addActionListener(event -> addLocation());
        deleteButton.addActionListener(event -> deleteSelectedLocation());
        refreshButton.addActionListener(event -> loadLocations());
        clearButton.addActionListener(event -> clearForm());
        totalValueButton.addActionListener(event -> calculateSelectedTotalValue());

        // Load whatever's already in the database the moment this
        // screen is created, so the table isn't empty until someone
        // remembers to click Refresh.
        loadLocations();
    }

    /**
     * Reads the form fields, and if they're valid, inserts a new
     * location. Any rejection (bad input here, or a rule broken in
     * HierarchyRules) shows as a popup message, never a crash.
     */
    private void addLocation() {

        String name = nameField.getText().trim();
        LocationType type = (LocationType) typeBox.getSelectedItem();
        String parentText = parentIdField.getText().trim();

        if (name.isEmpty()) {
            showError("Enter a name.");
            return;
        }

        // An empty Parent ID field means "no parent" (a WAREHOUSE).
        // Anything typed there needs to be a whole number, or we
        // reject it before it ever reaches the database.
        Integer parentId;
        if (parentText.isEmpty()) {
            parentId = null;
        } else {
            try {
                parentId = Integer.parseInt(parentText);
            } catch (NumberFormatException e) {
                showError("Parent ID must be a whole number, or left blank for a WAREHOUSE.");
                return;
            }
        }

        try {
            dao.insert(name, type, parentId);
            clearForm();
            loadLocations();
        } catch (IllegalArgumentException e) {
            // Validation rejections from HierarchyRules, e.g. wrong
            // parent type or a cycle, land here, not as a stack trace.
            showError(e.getMessage());
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    /**
     * Deletes whichever row is currently selected in the table. The
     * DAO itself refuses to delete a location that still has children,
     * that check happens there, not here, this method just displays
     * whatever the DAO decides.
     */
    private void deleteSelectedLocation() {

        int selectedRow = locationTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Select a location from the table first.");
            return;
        }

        // Column 0 is "ID" in the table, defined up in tableModel's
        // column names.
        int id = (int) tableModel.getValueAt(selectedRow, 0);

        try {
            dao.delete(id);
            loadLocations();
        } catch (IllegalStateException e) {
            // Thrown by the DAO when the location still has children.
            showError(e.getMessage());
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    /**
     * Runs the recursive calculation on whichever location is selected,
     * and shows the result in a popup. This is what proves the
     * recursion works on real data, not just in the unit tests.
     */
    private void calculateSelectedTotalValue() {

        int selectedRow = locationTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Select a location from the table first.");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);

        try {
            BigDecimal total = service.calculateTotalStockValue(id);
            JOptionPane.showMessageDialog(this,
                    "Total stock value under this location: " + total);
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    /**
     * Clears the table and refills it with whatever's currently in the
     * database. Called once when the screen first opens, and again
     * every time Add, Delete, or Refresh happens, so the table never
     * shows stale data.
     */
    private void loadLocations() {

        try {
            List<StorageLocation> locations = dao.findAll();

            // setRowCount(0) empties the table without rebuilding it
            // from scratch, faster than creating a whole new table
            // every refresh.
            tableModel.setRowCount(0);

            for (StorageLocation loc : locations) {
                tableModel.addRow(new Object[]{
                        loc.getId(),
                        loc.getName(),
                        loc.getType(),
                        loc.getParentId()
                });
            }
        } catch (SQLException e) {
            showError("Could not load locations: " + e.getMessage());
        }
    }

    /** Resets the form fields to empty and deselects any table row. */
    private void clearForm() {
        nameField.setText("");
        parentIdField.setText("");
        typeBox.setSelectedIndex(0);
        locationTable.clearSelection();
    }

    /** One place for showing an error popup, so every method above uses the same style of message. */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}