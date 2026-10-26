package com.fixmygadget.db;

import com.fixmygadget.exception.DaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
/**
 * Initializes the database schema and required reference data.
 *
 * <p>Loads the SQL script from {@code /db/schema.sql}, executes its
 * statements through JDBC, and logs successful initialization.
 * The script creates tables and indexes and inserts predefined
 * gadget types, features, and repair statuses when they are missing.</p>
 *
 * <p>Design approach: Database initialization component.
 * Uses try-with-resources to manage the input stream, JDBC connection,
 * and SQL statement safely.</p>
 *
 * <p>Provides a centralized entry point for preparing the database
 * structure required by the application and wraps initialization
 * failures in DaoException.</p>
 */
public class DatabaseInitializer {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    public void init() {
        try (InputStream in = getClass().getResourceAsStream("/db/schema.sql")) {
            if (in == null) {
                throw new DaoException("db/schema.sql not found");
            }
            String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            try (Connection connection = ConnectionProvider.getInstance().getConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute(sql);
            }
            log.info("Database initialized");
        } catch (IOException | SQLException e) {
            throw new DaoException("Database initialization failed", e);
        }
    }
}