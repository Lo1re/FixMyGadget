package com.fixmygadget.dao.jdbc;

import com.fixmygadget.db.ConnectionProvider;
import com.fixmygadget.exception.DaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
/**
 * Provides shared JDBC-based data retrieval functionality for DAO
 * implementations.
 *
 * <p>Stores a base SELECT query and implements a reusable selection
 * mechanism that binds query parameters, executes SQL, iterates over
 * the result set, and converts rows into domain objects through the
 * abstract {@code map} method.</p>
 *
 * <p>Design patterns: Template Method and Data Access Object (DAO).
 * The abstract mapping method delegates entity-specific row conversion
 * to subclasses while the base class handles the common query workflow.</p>
 *
 * <p>Promotes code reuse across JDBC DAO implementations, centralizes
 * resource management through try-with-resources, and wraps SQL
 * failures in {@link com.fixmygadget.exception.DaoException}.</p>
 */
public abstract class AbstractJdbcDao<T> {
    private static final Logger log = LoggerFactory.getLogger(AbstractJdbcDao.class);

    private final String baseSelect;

    protected AbstractJdbcDao(String baseSelect) {
        this.baseSelect = baseSelect;
    }
    // one row to object
    protected abstract T map(ResultSet rs) throws SQLException;

    //standalone select
    protected List<T> select(String where, Object... params) {
        String sql = baseSelect + " " + where;
        log.debug(sql);
        List<T> result = new ArrayList<>();
        try (Connection connection = ConnectionProvider.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Cannot read data", e);
        }
        return result;
    }
}