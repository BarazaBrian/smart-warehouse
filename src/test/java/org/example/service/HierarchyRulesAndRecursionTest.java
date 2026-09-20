package org.example.service;

import org.example.model.LocationType;
import org.example.model.StorageLocation;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HierarchyRulesAndRecursionTest {

    // --- Recursion: base case ---

    @Test
    void leafWithNoChildrenReturnsItsOwnDirectValueOnly() {
        StorageLocation shelf = new StorageLocation(1, "Shelf A", LocationType.SHELF, 2);
        shelf.setDirectStockValue(new BigDecimal("150.00"));

        assertEquals(0, new BigDecimal("150.00").compareTo(shelf.calculateTotalStockValue()),
            "Base case: a location with no children should return only its own value");
    }

    // --- Recursion: genuine depth (warehouse -> zone -> shelf) ---

    @Test
    void recursionSumsValueAcrossFullThreeLevelDepth() {
        StorageLocation warehouse = new StorageLocation(1, "Main Warehouse", LocationType.WAREHOUSE, null);
        warehouse.setDirectStockValue(BigDecimal.ZERO);

        StorageLocation zoneA = new StorageLocation(2, "Zone A", LocationType.ZONE, 1);
        zoneA.setDirectStockValue(new BigDecimal("50.00"));
        StorageLocation zoneB = new StorageLocation(3, "Zone B", LocationType.ZONE, 1);
        zoneB.setDirectStockValue(new BigDecimal("30.00"));

        StorageLocation shelfA1 = new StorageLocation(4, "Shelf A1", LocationType.SHELF, 2);
        shelfA1.setDirectStockValue(new BigDecimal("200.00"));
        StorageLocation shelfA2 = new StorageLocation(5, "Shelf A2", LocationType.SHELF, 2);
        shelfA2.setDirectStockValue(new BigDecimal("75.00"));
        StorageLocation shelfB1 = new StorageLocation(6, "Shelf B1", LocationType.SHELF, 3);
        shelfB1.setDirectStockValue(new BigDecimal("100.00"));

        zoneA.addChild(shelfA1);
        zoneA.addChild(shelfA2);
        zoneB.addChild(shelfB1);
        warehouse.addChild(zoneA);
        warehouse.addChild(zoneB);

        // 0 + 50 + 30 (zones) + 200 + 75 + 100 (shelves) = 455.00
        assertEquals(0, new BigDecimal("455.00").compareTo(warehouse.calculateTotalStockValue()),
            "Recursion should aggregate direct values across all three levels, not just direct children");
    }

    @Test
    void zoneWithNoShelvesYetSumsOnlyItsOwnValue() {
        StorageLocation zone = new StorageLocation(2, "Empty Zone", LocationType.ZONE, 1);
        zone.setDirectStockValue(BigDecimal.ZERO);

        assertEquals(0, BigDecimal.ZERO.compareTo(zone.calculateTotalStockValue()),
            "A zone with no shelves added yet should sum to zero, not throw or return its parent's value");
    }

    // --- Validation: parent-type rules ---

    @Test
    void warehouseWithNoParentIsValid() {
        assertDoesNotThrow(() -> HierarchyRules.validateParentType(LocationType.WAREHOUSE, null));
    }

    @Test
    void warehouseWithAParentIsRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> HierarchyRules.validateParentType(LocationType.WAREHOUSE, LocationType.ZONE));
    }

    @Test
    void zoneWithWarehouseParentIsValid() {
        assertDoesNotThrow(() -> HierarchyRules.validateParentType(LocationType.ZONE, LocationType.WAREHOUSE));
    }

    @Test
    void zoneWithNoParentIsRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> HierarchyRules.validateParentType(LocationType.ZONE, null));
    }

    @Test
    void zoneWithShelfParentIsRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> HierarchyRules.validateParentType(LocationType.ZONE, LocationType.SHELF));
    }

    @Test
    void shelfWithZoneParentIsValid() {
        assertDoesNotThrow(() -> HierarchyRules.validateParentType(LocationType.SHELF, LocationType.ZONE));
    }

    @Test
    void shelfWithWarehouseParentIsRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> HierarchyRules.validateParentType(LocationType.SHELF, LocationType.WAREHOUSE));
    }

    // --- Validation: cycle prevention ---

    @Test
    void assigningAnAncestorAsParentIsDetectedAsACycle() {
        Map<Integer, Integer> parentIdByLocationId = new HashMap<>();
        parentIdByLocationId.put(3, 2);
        parentIdByLocationId.put(2, 1);
        parentIdByLocationId.put(1, null);

        assertTrue(HierarchyRules.wouldCreateCycle(3, 1, parentIdByLocationId),
            "Setting location 1's parent to 3 should be a cycle, since 3 descends from 1");
    }

    @Test
    void unrelatedParentAssignmentIsNotACycle() {
        Map<Integer, Integer> parentIdByLocationId = new HashMap<>();
        parentIdByLocationId.put(2, 1);
        parentIdByLocationId.put(1, null);

        assertFalse(HierarchyRules.wouldCreateCycle(1, 2, parentIdByLocationId),
            "Assigning an unrelated valid parent should not be flagged as a cycle");
    }

    @Test
    void newlyInsertedLocationCanNeverCreateACycle() {
        Map<Integer, Integer> parentIdByLocationId = new HashMap<>();
        parentIdByLocationId.put(1, null);

        assertFalse(HierarchyRules.wouldCreateCycle(1, null, parentIdByLocationId),
            "selfId is null on insert, a new row cannot yet be anyone's ancestor");
    }
}
