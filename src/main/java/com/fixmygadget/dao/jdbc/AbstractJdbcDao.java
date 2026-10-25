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