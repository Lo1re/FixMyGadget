package com.fixmygadget.db;

import com.fixmygadget.exception.DaoException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class ConnectionProvider {
    private static ConnectionProvider instance;

    private final String url;
    private final String password;
    private final String user;


    private ConnectionProvider() {
        try (InputStream in = getClass().getResourceAsStream("/application.properties")) {
            if (in == null) {
                throw new DaoException("application.properties not found");
            }
            Properties props = new Properties();
            props.load(in);
            this.url = props.getProperty("db.url");
            this.user = props.getProperty("db.user");
            this.password = props.getProperty("db.password");
        } catch (IOException e) {
            throw new DaoException("Cannot read application.properties", e);
        }
    }
    public static ConnectionProvider getInstance() {
        if (instance == null) {
            instance = new ConnectionProvider();
        }
        return instance;
    }
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new DaoException("Cannot connect to database", e);
        }
    }

}
