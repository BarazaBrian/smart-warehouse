package org.example.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Composite pattern participant. One class represents every level of
 * the hierarchy (warehouse, zone, shelf), a leaf (shelf, no children)
 * and a composite (warehouse or zone, with children) are treated the
 * same way, which is the whole point of this pattern.
 *
 * Stock values are BigDecimal, not double, to match Product.unitPrice.
 * Money should never be stored as double, rounding errors in double
 * arithmetic can make totals slightly wrong, BigDecimal avoids that.
 */
public class StorageLocation {

    private int id;
    private String name;
    private LocationType type;
    private Integer parentId; // null for a WAREHOUSE
    private final List<StorageLocation> children = new ArrayList<>();

    // Direct stock value at this location only, set by the service
    // layer from InventoryItem/Product data before recursion runs.
    private BigDecimal directStockValue = BigDecimal.ZERO;

    public StorageLocation(int id, String name, LocationType type, Integer parentId) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.parentId = parentId;
    }

    public void addChild(StorageLocation child) {
        children.add(child);
    }

    /**
     * Recursively sums the stock value held at this location and every
     * descendant location.
     *
     * Base case: a location with no children (always true for SHELF)
     * returns just its own direct stock value, the loop below has
     * nothing to iterate over.
     *
     * Depth is bounded structurally: only three levels exist, and
     * StorageLocationDAO refuses to create a fourth level or a cycle,
     * so this recursion can't run away.
     */
    public BigDecimal calculateTotalStockValue() {
        BigDecimal total = directStockValue;
        for (StorageLocation child : children) {
            total = total.add(child.calculateTotalStockValue());
        }
        return total;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public LocationType getType() { return type; }
    public Integer getParentId() { return parentId; }
    public List<StorageLocation> getChildren() { return children; }
    public BigDecimal getDirectStockValue() { return directStockValue; }
    public void setDirectStockValue(BigDecimal value) { this.directStockValue = value; }

    @Override
    public String toString() {
        return type + ": " + name + " (id=" + id + ")";
    }
}
