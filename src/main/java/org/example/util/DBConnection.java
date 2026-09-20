package org.example.util;

import com.mysql.cj.jdbc.MysqlDataSource;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Builds the app's database connection from db.properties.
 *
 * Two things are offered here, for two different needs in the codebase:
 *
 * getConnection() returns a brand new connection each time it's
 * called. Used by StorageLocationDAO already, and by InventoryService
 * and StockMovementService, which each open one, use it, and close it
 * again inside a single method call.
 *
 * getDataSource() returns the DataSource object itself, not a
 * connection. A DataSource is just an object whose whole job is to
 * hand out connections on request, InventoryService and
 * StockMovementService's constructors ask for this directly, and call
 * .getConnection() on it themselves whenever they need one.
 *
 * ProductService is different again, it wants DAOs that were already
 * built using ONE long-lived connection, kept open for as long as the
 * app runs. That connection should be created once in Main.java by
 * calling getConnection() a single time and holding onto the result,
 * not built here.
 */
public class DBConnection {

    private static final String CONFIG_FILE = "db.properties";

    // Built once, the first time it's needed, then reused. This is
    // safe because MysqlDataSource itself doesn't hold a connection
    // open, it's just a small factory object, not a live connection.
    private static DataSource dataSource;

    public static DataSource getDataSource() throws SQLException {
        if (dataSource == null) {
            dataSource = buildDataSource();
        }
        return dataSource;
    }

    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    private static DataSource buildDataSource() throws SQLException {
        Properties props = loadProperties();

        MysqlDataSource ds = new MysqlDataSource();
        ds.setUrl(props.getProperty("db.url"));
        ds.setUser(props.getProperty("db.user"));
        ds.setPassword(props.getProperty("db.password"));

        return ds;
    }

    private static Properties loadProperties() throws SQLException {
        Properties props = new Properties();

        try (FileInputStream input = new FileInputStream(CONFIG_FILE)) {
            props.load(input);
        } catch (IOException e) {
            throw new SQLException(
                "Could not read " + CONFIG_FILE + ". Make sure this file exists in the project's root folder.", e);
        }

        return props;
    }
}
