package com.fixmygadget.dao.jdbc;

import com.fixmygadget.dao.GadgetDao;
import com.fixmygadget.db.ConnectionProvider;
import com.fixmygadget.exception.DaoException;
import com.fixmygadget.model.Feature;
import com.fixmygadget.model.Gadget;
import com.fixmygadget.model.GadgetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
/**
 * Implements JDBC-based persistence and retrieval for gadget entities.
 *
 * <p>Manages gadget records, their manufacturer associations, and
 * their features stored in the related database tables. Supports
 * filtering by manufacturer, type, name and type, and screen dimensions.</p>
 *
 * <p>Design patterns: Data Access Object (DAO) and Template Method
 * through {@link AbstractJdbcDao}. Overrides selection to load gadget
 * features after mapping the main database records.</p>
 *
 * <p>Uses JDBC transactions to keep gadget and feature changes
 * consistent during save and update operations. Resolves manufacturer
 * identifiers, persists feature associations, maps result rows into
 * {@code Gadget} objects, and wraps SQL failures in
 * {@link com.fixmygadget.exception.DaoException}.</p>
 */
public class JdbcGadgetDao extends AbstractJdbcDao<Gadget> implements GadgetDao {
    private static final Logger log = LoggerFactory.getLogger(JdbcGadgetDao.class);

    public JdbcGadgetDao() {
        super("SELECT g.id, g.name, m.name AS manufacturer, g.price, g.type_code, "
                + "g.screen_width, g.screen_height "
                + "FROM gadget g JOIN manufacturer m ON m.id = g.manufacturer_id");
    }

    @Override
    public void save(Gadget gadget) {
        try (Connection connection = ConnectionProvider.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try {
                int manufacturerId = getOrCreateManufacturer(connection, gadget.getManufacturer());
                insertGadget(connection, gadget, manufacturerId);
                insertFeatures(connection, gadget);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DaoException("Cannot save gadget", e);
        }
    }

    @Override
    public void update(Gadget gadget) {
        try (Connection connection = ConnectionProvider.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try {
                int manufacturerId = getOrCreateManufacturer(connection, gadget.getManufacturer());
                updateGadget(connection, gadget, manufacturerId);
                deleteFeatures(connection, gadget.getId());
                insertFeatures(connection, gadget);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DaoException("Cannot update gadget", e);
        }
    }

    @Override
    public void delete(Integer id) {
        // gadget_feature oN DELETE CASCADE
        String sql = "DELETE FROM gadget WHERE id = ?";
        log.debug(sql);
        try (Connection connection = ConnectionProvider.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Cannot delete gadget", e);
        }
    }

    @Override
    public Gadget findById(Integer id) {
        List<Gadget> list = select("WHERE g.id = ?", id);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public List<Gadget> findAll() {
        return select("");
    }

    @Override
    public List<Gadget> findByManufacturer(String manufacturer) {
        return select("WHERE m.name = ?", manufacturer);
    }

    @Override
    public List<Gadget> findByType(GadgetType type) {
        return select("WHERE g.type_code = ?", type.name());
    }

    @Override
    public List<Gadget> findByNameAndType(String name, GadgetType type) {
        String pattern = "%" + name.toLowerCase() + "%";
        return select("WHERE LOWER(g.name) LIKE ? AND g.type_code = ?", pattern, type.name());
    }

    @Override
    public List<Gadget> findByScreenSize(int x1, int y1, int x2, int y2) {
        return select("WHERE g.screen_width BETWEEN ? AND ? AND g.screen_height BETWEEN ? AND ?",
                x1, x2, y1, y2);
    }

    @Override
    protected List<Gadget> select(String where, Object... params) {
        List<Gadget> gadgets = super.select(where, params);
        for (Gadget gadget : gadgets) {
            loadFeatures(gadget);
        }
        return gadgets;
    }

    @Override
    protected Gadget map(ResultSet rs) throws SQLException {
        Gadget gadget = new Gadget();
        gadget.setId(rs.getInt("id"));
        gadget.setName(rs.getString("name"));
        gadget.setManufacturer(rs.getString("manufacturer"));
        gadget.setPrice(rs.getBigDecimal("price"));
        gadget.setType(GadgetType.valueOf(rs.getString("type_code")));
        gadget.setScreenWidth(rs.getInt("screen_width"));
        gadget.setScreenHeight(rs.getInt("screen_height"));
        return gadget;
    }

    private void loadFeatures(Gadget gadget) {
        String sql = "SELECT feature_code FROM gadget_feature WHERE gadget_id = ?";
        log.debug(sql);
        try (Connection connection = ConnectionProvider.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, gadget.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    gadget.addFeature(Feature.valueOf(rs.getString("feature_code")));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Cannot read gadget features", e);
        }
    }

    private int getOrCreateManufacturer(Connection connection, String name) throws SQLException {
        String selectSql = "SELECT id FROM manufacturer WHERE name = ?";
        log.debug(selectSql);
        try (PreparedStatement ps = connection.prepareStatement(selectSql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        String insertSql = "INSERT INTO manufacturer (name) VALUES (?) RETURNING id";
        log.debug(insertSql);
        try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private void insertGadget(Connection connection, Gadget gadget, int manufacturerId) throws SQLException {
        String sql = "INSERT INTO gadget (name, manufacturer_id, price, type_code, screen_width, screen_height) "
                + "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        log.debug(sql);
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, gadget.getName());
            ps.setInt(2, manufacturerId);
            ps.setBigDecimal(3, gadget.getPrice());
            ps.setString(4, gadget.getType().name());
            ps.setInt(5, gadget.getScreenWidth());
            ps.setInt(6, gadget.getScreenHeight());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                gadget.setId(rs.getInt(1));
            }
        }
    }

    private void updateGadget(Connection connection, Gadget gadget, int manufacturerId) throws SQLException {
        String sql = "UPDATE gadget SET name = ?, manufacturer_id = ?, price = ?, type_code = ?, "
                + "screen_width = ?, screen_height = ? WHERE id = ?";
        log.debug(sql);
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, gadget.getName());
            ps.setInt(2, manufacturerId);
            ps.setBigDecimal(3, gadget.getPrice());
            ps.setString(4, gadget.getType().name());
            ps.setInt(5, gadget.getScreenWidth());
            ps.setInt(6, gadget.getScreenHeight());
            ps.setInt(7, gadget.getId());
            ps.executeUpdate();
        }
    }

    private void deleteFeatures(Connection connection, int gadgetId) throws SQLException {
        String sql = "DELETE FROM gadget_feature WHERE gadget_id = ?";
        log.debug(sql);
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, gadgetId);
            ps.executeUpdate();
        }
    }

    private void insertFeatures(Connection connection, Gadget gadget) throws SQLException {
        String sql = "INSERT INTO gadget_feature (gadget_id, feature_code) VALUES (?, ?)";
        log.debug(sql);
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (Feature feature : gadget.getFeatures()) {
                ps.setInt(1, gadget.getId());
                ps.setString(2, feature.name());
                ps.executeUpdate();
            }
        }
    }
}