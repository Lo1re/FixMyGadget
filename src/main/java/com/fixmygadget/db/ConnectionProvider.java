package com.fixmygadget.db;

import com.fixmygadget.exception.DaoException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
/**
 * Provides database connections using a pooled JDBC data source.
 *
 * <p>Loads database connection properties from
 * {@code application.properties}, configures a HikariCP connection
 * pool, and exposes connections to DAO implementations.</p>
 *
 * <p>Design patterns: Singleton and Connection Pool.
 * The synchronized {@code getInstance()} method ensures that only one
 * ConnectionProvider instance is initialized, while HikariCP manages
 * the reuse of database connections.</p>
 *
 * <p>Centralizes database connection configuration, reduces the
 * overhead of repeatedly establishing physical connections, and
 * translates connection-related failures into DaoException.</p>
 */
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