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