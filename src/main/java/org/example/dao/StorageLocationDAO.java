package org.example.dao;

import org.example.model.LocationType;
import org.example.model.StorageLocation;
import org.example.service.HierarchyRules;
import org.example.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO = Data Access Object. This is the only class allowed to write
 * SQL for storage locations. Everything else asks this class for data
 * instead of talking to the database directly.
 *
 * PreparedStatement: a SQL sentence with "?" blanks, filled in
 * afterwards with setString(), setInt(), etc. Safer than joining text
 * together ourselves, that's how SQL injection happens.
 *
 * try (Connection conn = ...) { ... }: try-with-resources, whatever is
 * opened in the parentheses gets closed automatically when the block
 * ends, even if an error happens partway through.
 */
public class StorageLocationDAO {

    /**
     * A brand-new location can never already be part of the tree, so
     * there's nothing to check for a cycle here, unlike update(),
     * where the location already exists and could theoretically be
     * re-parented into its own descendant.
     */
    public int insert(String name, LocationType type, Integer parentId) throws SQLException {

        validateParentType(type, parentId);

        String sql = "INSERT INTO storage_location (name, type, parent_id) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(2, type.name());

            if (parentId == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, parentId);
            }

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        throw new SQLException("Insert failed, no generated key returned");
    }

    public void update(int id, String name, LocationType type, Integer parentId) throws SQLException {

        validateParentType(type, parentId);
        if (parentId != null && wouldCreateCycle(parentId, id)) {
            throw new IllegalArgumentException("Parent assignment would create a cycle");
        }

        String sql = "UPDATE storage_location SET name = ?, type = ?, parent_id = ? WHERE location_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, type.name());

            if (parentId == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, parentId);
            }

            ps.setInt(4, id);
            ps.executeUpdate();
        }
    }

    /**
     * TODO: once Brian's InventoryItem DAO exists, also check whether
     * inventory is stored at this location before allowing delete.
     */
    public void delete(int id) throws SQLException {

        if (!findChildren(id).isEmpty()) {
            throw new IllegalStateException("Cannot delete a location that still has children");
        }

        String sql = "DELETE FROM storage_location WHERE location_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public StorageLocation findById(int id) throws SQLException {

        String sql = "SELECT location_id, name, type, parent_id FROM storage_location WHERE location_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }

        return null;
    }

    public List<StorageLocation> findChildren(int parentId) throws SQLException {

        String sql = "SELECT location_id, name, type, parent_id FROM storage_location WHERE parent_id = ?";
        List<StorageLocation> results = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, parentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }

        return results;
    }

    /**
     * Returns every location, not just one level, for showing in a
     * table on screen.
     */
    public List<StorageLocation> findAll() throws SQLException {

        String sql = "SELECT location_id, name, type, parent_id FROM storage_location ORDER BY type, name";
        List<StorageLocation> results = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                results.add(mapRow(rs));
            }
        }

        return results;
    }

    private StorageLocation mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("location_id");
        String name = rs.getString("name");
        LocationType type = LocationType.valueOf(rs.getString("type"));

        int parentIdRaw = rs.getInt("parent_id");
        Integer parentId = rs.wasNull() ? null : parentIdRaw;

        return new StorageLocation(id, name, type, parentId);
    }

    private void validateParentType(LocationType type, Integer parentId) throws SQLException {
        LocationType parentType = null;

        if (parentId != null) {
            StorageLocation parent = findById(parentId);
            if (parent == null) {
                throw new IllegalArgumentException("Parent location does not exist");
            }
            parentType = parent.getType();
        }

        HierarchyRules.validateParentType(type, parentType);
    }

    private boolean wouldCreateCycle(Integer proposedParentId, Integer selfId) throws SQLException {
        if (selfId == null || proposedParentId == null) {
            return false;
        }

        Map<Integer, Integer> parentIdByLocationId = new HashMap<>();
        Integer current = proposedParentId;

        while (current != null && !parentIdByLocationId.containsKey(current)) {
            StorageLocation loc = findById(current);
            if (loc == null) {
                break;
            }
            parentIdByLocationId.put(current, loc.getParentId());
            current = loc.getParentId();
        }

        return HierarchyRules.wouldCreateCycle(proposedParentId, selfId, parentIdByLocationId);
    }
}