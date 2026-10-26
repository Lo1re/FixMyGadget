package com.fixmygadget.db;

import com.fixmygadget.exception.DaoException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public final class ConnectionProvider {
    private static ConnectionProvider instance;


    private final HikariDataSource dataSource;

    private ConnectionProvider() {
        try (InputStream in = getClass().getResourceAsStream("/application.properties")) {
            if (in == null) {
                throw new DaoException("application.properties not found");
            }
            Properties props = new Properties();
            props.load(in);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.user"));
            config.setPassword(props.getProperty("db.password"));
            config.setMaximumPoolSize(5);
            config.setMinimumIdle(2);
            this.dataSource = new HikariDataSource(config);
        } catch (IOException e) {
            throw new DaoException("Cannot read application.properties", e);
        }
    }

    public static synchronized ConnectionProvider getInstance() {
        if (instance == null) {
            instance = new ConnectionProvider();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DaoException("Cannot connect to database", e);
        }
    }
}