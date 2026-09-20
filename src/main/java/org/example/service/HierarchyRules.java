package org.example.service;

import org.example.model.LocationType;
import java.util.Map;

/**
 * Checks two rules about the storage hierarchy, without needing a
 * database connection, so these rules can be unit tested on their own.
 */
public class HierarchyRules {

    /**
     * WAREHOUSE must have no parent. ZONE's parent must be a WAREHOUSE.
     * SHELF's parent must be a ZONE. parentType is null when there is
     * no parent.
     */
    public static void validateParentType(LocationType type, LocationType parentType) {

        if (type == LocationType.WAREHOUSE) {
            if (parentType != null) {
                throw new IllegalArgumentException("A WAREHOUSE cannot have a parent");
            }
            return;
        }

        if (type == LocationType.ZONE) {
            if (parentType != LocationType.WAREHOUSE) {
                throw new IllegalArgumentException("A ZONE's parent must be a WAREHOUSE");
            }
            return;
        }

        if (type == LocationType.SHELF) {
            if (parentType != LocationType.ZONE) {
                throw new IllegalArgumentException("A SHELF's parent must be a ZONE");
            }
            return;
        }
    }

    /**
     * Only a SHELF may directly hold inventory. A warehouse or a zone
     * should never have a product assigned straight to it, only to a
     * shelf somewhere inside it.
     *
     * Brian's InventoryItemDAO.assignProductToLocation() should call
     * this before saving, the same way StorageLocationDAO calls
     * validateParentType() before saving a location.
     */
    public static boolean isValidInventoryLocation(LocationType type) {
        return type == LocationType.SHELF;
    }

    /**
     * Checks whether making proposedParentId the parent of selfId would
     * create a loop. Starts at the proposed parent and walks upward
     * using the lookup table (locationId -> its parentId). If selfId
     * shows up anywhere in that chain, the assignment would make a
     * location its own ancestor.
     *
     * selfId is null when inserting a brand new location, and a new
     * location can never already be part of the tree, so it can never
     * create a loop.
     */
    public static boolean wouldCreateCycle(Integer proposedParentId, Integer selfId,
                                            Map<Integer, Integer> parentIdByLocationId) {

        if (selfId == null || proposedParentId == null) {
            return false;
        }

        Integer current = proposedParentId;

        while (current != null) {
            if (current.equals(selfId)) {
                return true;
            }
            current = parentIdByLocationId.get(current);
        }

        return false;
    }
}
