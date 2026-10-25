package com.fixmygadget.dao.jdbc;

import com.fixmygadget.dao.CustomerDao;
import com.fixmygadget.db.ConnectionProvider;
import com.fixmygadget.exception.DaoException;
import com.fixmygadget.model.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class JdbcCustomerDao extends AbstractJdbcDao<Customer> implements CustomerDao {
    private static final Logger log = LoggerFactory.getLogger(JdbcCustomerDao.class);

    public JdbcCustomerDao() {
        super("SELECT id, last_name, first_name, phone FROM customer");
    }

    @Override
    public void save(Customer customer) {
        String sql = "INSERT INTO customer (last_name, first_name, phone) VALUES (?, ?, ?) RETURNING id";
        log.debug(sql);
        try (Connection connection = ConnectionProvider.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, customer.getLastName());
            ps.setString(2, customer.getFirstName());
            ps.setString(3, customer.getPhone());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                customer.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new DaoException("Cannot save customer", e);
        }
    }

    @Override
    public void update(Customer customer) {
        String sql = "UPDATE customer SET last_name = ?, first_name = ?, phone = ? WHERE id = ?";
        log.debug(sql);
        try (Connection connection = ConnectionProvider.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, customer.getLastName());
            ps.setString(2, customer.getFirstName());
            ps.setString(3, customer.getPhone());
            ps.setInt(4, customer.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Cannot update customer", e);
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM customer WHERE id = ?";
        log.debug(sql);
        try (Connection connection = ConnectionProvider.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Cannot delete customer", e);
        }
    }

    @Override
    public Customer findById(Integer id) {
        List<Customer> list = select("WHERE id = ?", id);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public Customer findByPhone(String phone) {
        List<Customer> list = select("WHERE phone = ?", phone);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public List<Customer> findAll() {
        return select("");
    }

    @Override
    protected Customer map(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setId(rs.getInt("id"));
        customer.setLastName(rs.getString("last_name"));
        customer.setFirstName(rs.getString("first_name"));
        customer.setPhone(rs.getString("phone"));
        return customer;
    }
}