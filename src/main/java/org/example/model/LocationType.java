package org.example.model;

/**
 * The three permitted levels of the storage hierarchy.
 * Order matters: WAREHOUSE, then ZONE, then SHELF.
 */
public enum LocationType {
    WAREHOUSE,
    ZONE,
    SHELF
}
