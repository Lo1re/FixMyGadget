package com.fixmygadget.dao.jdbc;

import com.fixmygadget.dao.CustomerDao;
import com.fixmygadget.dao.GadgetDao;
import com.fixmygadget.dao.RepairOrderDao;
import com.fixmygadget.dao.TechnicianDao;
import com.fixmygadget.db.ConnectionProvider;
import com.fixmygadget.exception.DaoException;
import com.fixmygadget.model.RepairOrder;
import com.fixmygadget.model.RepairStatus;
import com.fixmygadget.model.Technician;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;
/**
 * Implements JDBC-based persistence and retrieval for repair orders.
 *
 * <p>Provides CRUD operations and queries for finding repair orders
 * by status, technician, customer, gadget, creation period, and
 * completion period. Maps database records to {@code RepairOrder}
 * objects and resolves associated gadgets, customers, and technicians
 * through their respective DAO interfaces.</p>
 *
 * <p>Design patterns: Data Access Object (DAO) and Template Method
 * through {@link AbstractJdbcDao}. Uses constructor injection to
 * receive the related DAO dependencies.</p>
 *
 * <p>Handles nullable technician assignments and completion
 * timestamps, converts database timestamps to {@code LocalDateTime},
 * and binds order fields to prepared statements. Keeps persistence
 * logic separate from repair-order business logic and wraps SQL
 * failures in {@link com.fixmygadget.exception.DaoException}.</p>
 */
public class JdbcRepairOrderDao extends AbstractJdbcDao<RepairOrder> implements RepairOrderDao {
    private static final Logger log = LoggerFactory.getLogger(JdbcRepairOrderDao.class);

    private final GadgetDao gadgetDao;
    private final CustomerDao customerDao;
    private final TechnicianDao technicianDao;

    public JdbcRepairOrderDao(GadgetDao gadgetDao, CustomerDao customerDao, TechnicianDao technicianDao) {
        super("SELECT id, gadget_id, customer_id, technician_id, problem, status_code, "
                + "cost, created_at, finished_at FROM repair_order");
        this.gadgetDao = gadgetDao;
        this.customerDao = customerDao;
        this.technicianDao = technicianDao;
    }


    @Override
    public void save(RepairOrder order) {
        String sql = "INSERT INTO repair_order (gadget_id, customer_id, technician_id, problem, "
                + "status_code, cost, created_at, finished_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        log.debug(sql);
        try (Connection connection = ConnectionProvider.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            fillStatement(ps, order);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                order.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new DaoException("Cannot save repair order", e);
        }
    }

    @Override
    public void update(RepairOrder order) {
        String sql = "UPDATE repair_order SET gadget_id = ?, customer_id = ?, technician_id = ?, "
                + "problem = ?, status_code = ?, cost = ?, created_at = ?, finished_at = ? WHERE id = ?";
        log.debug(sql);
        try (Connection connection = ConnectionProvider.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            fillStatement(ps, order);
            ps.setInt(9, order.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Cannot update repair order", e);
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM repair_order WHERE id = ?";
        log.debug(sql);
        try (Connection connection = ConnectionProvider.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Cannot delete repair order", e);
        }
    }


    @Override
    public RepairOrder findById(Integer id) {
        List<RepairOrder> list = select("WHERE id = ?", id);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public List<RepairOrder> findAll() {
        return select("");
    }

    @Override
    public List<RepairOrder> findByStatus(RepairStatus status) {
        return select("WHERE status_code = ?", status.name());
    }

    @Override
    public List<RepairOrder> findByTechnician(int technicianId) {
        return select("WHERE technician_id = ?", technicianId);
    }

    @Override
    public List<RepairOrder> findByCustomer(int customerId) {
        return select("WHERE customer_id = ?", customerId);
    }

    @Override
    public List<RepairOrder> findByGadget(int gadgetId) {
        return select("WHERE gadget_id = ?", gadgetId);
    }

    @Override
    public List<RepairOrder> findByPeriod(LocalDateTime from, LocalDateTime to) {
        return select("WHERE created_at BETWEEN ? AND ?", Timestamp.valueOf(from), Timestamp.valueOf(to));
    }

    @Override
    public List<RepairOrder> findFinishedBetween(LocalDateTime from, LocalDateTime to) {
        return select("WHERE status_code = 'DONE' AND finished_at BETWEEN ? AND ?",
                Timestamp.valueOf(from), Timestamp.valueOf(to));
    }


    @Override
    protected RepairOrder map(ResultSet rs) throws SQLException {
        RepairOrder order = new RepairOrder();
        order.setId(rs.getInt("id"));
        order.setGadget(gadgetDao.findById(rs.getInt("gadget_id")));
        order.setCustomer(customerDao.findById(rs.getInt("customer_id")));

        int technicianId = rs.getInt("technician_id");
        if (!rs.wasNull()) {
            Technician technician = technicianDao.findById(technicianId);
            order.setTechnician(technician);
        }

        order.setProblem(rs.getString("problem"));
        order.setStatus(RepairStatus.valueOf(rs.getString("status_code")));
        order.setCost(rs.getBigDecimal("cost"));
        order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

        Timestamp finished = rs.getTimestamp("finished_at");
        if (finished != null) {
            order.setFinishedAt(finished.toLocalDateTime());
        }
        return order;
    }

    private void fillStatement(PreparedStatement ps, RepairOrder order) throws SQLException {
        ps.setInt(1, order.getGadget().getId());
        ps.setInt(2, order.getCustomer().getId());
        if (order.getTechnician() == null) {
            ps.setNull(3, Types.INTEGER);
        } else {
            ps.setInt(3, order.getTechnician().getId());
        }
        ps.setString(4, order.getProblem());
        ps.setString(5, order.getStatus().name());
        ps.setBigDecimal(6, order.getCost());
        ps.setTimestamp(7, Timestamp.valueOf(order.getCreatedAt()));
        if (order.getFinishedAt() == null) {
            ps.setNull(8, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(8, Timestamp.valueOf(order.getFinishedAt()));
        }
    }
}