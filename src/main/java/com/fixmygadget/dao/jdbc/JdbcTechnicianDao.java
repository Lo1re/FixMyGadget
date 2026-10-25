package com.fixmygadget.dao.jdbc;

import com.fixmygadget.dao.TechnicianDao;
import com.fixmygadget.db.ConnectionProvider;
import com.fixmygadget.exception.DaoException;
import com.fixmygadget.model.Technician;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class JdbcTechnicianDao extends AbstractJdbcDao<Technician> implements TechnicianDao {
    private static final Logger log = LoggerFactory.getLogger(JdbcTechnicianDao.class);

    public JdbcTechnicianDao() {
        super("SELECT id, last_name, first_name FROM technician");
    }

    @Override
    public void save(Technician technician) {
        String sql = "INSERT INTO technician (last_name, first_name) VALUES (?, ?) RETURNING id";
        log.debug(sql);
        try (Connection connection = ConnectionProvider.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, technician.getLastName());
            ps.setString(2, technician.getFirstName());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                technician.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new DaoException("Cannot save technician", e);
        }
    }

    @Override
    public void update(Technician technician) {
        String sql = "UPDATE technician SET last_name = ?, first_name = ? WHERE id = ?";
        log.debug(sql);
        try (Connection connection = ConnectionProvider.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, technician.getLastName());
            ps.setString(2, technician.getFirstName());
            ps.setInt(3, technician.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Cannot update technician", e);
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM technician WHERE id = ?";
        log.debug(sql);
        try (Connection connection = ConnectionProvider.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Cannot delete technician", e);
        }
    }

    @Override
    public Technician findById(Integer id) {
        List<Technician> list = select("WHERE id = ?", id);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public List<Technician> findAll() {
        return select("");
    }

    @Override
    protected Technician map(ResultSet rs) throws SQLException {
        Technician technician = new Technician();
        technician.setId(rs.getInt("id"));
        technician.setLastName(rs.getString("last_name"));
        technician.setFirstName(rs.getString("first_name"));
        return technician;
    }
}