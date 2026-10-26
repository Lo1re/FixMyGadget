package dao.jdbc;

import com.fixmygadget.dao.CustomerDao;
import com.fixmygadget.dao.GadgetDao;
import com.fixmygadget.dao.TechnicianDao;
import com.fixmygadget.dao.jdbc.JdbcRepairOrderDao;
import com.fixmygadget.db.ConnectionProvider;
import com.fixmygadget.exception.DaoException;
import com.fixmygadget.model.Customer;
import com.fixmygadget.model.Gadget;
import com.fixmygadget.model.GadgetBuilder;
import com.fixmygadget.model.GadgetType;
import com.fixmygadget.model.RepairOrder;
import com.fixmygadget.model.RepairOrderBuilder;
import com.fixmygadget.model.RepairStatus;
import com.fixmygadget.model.Technician;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class JdbcRepairOrderDaoTest {

    private static final LocalDateTime CREATED = LocalDateTime.of(2026, 10, 1, 9, 30);
    private static final LocalDateTime FINISHED = LocalDateTime.of(2026, 10, 3, 17, 0);

    private MockedStatic<ConnectionProvider> providerStatic;
    private Connection connection;
    private PreparedStatement ps;
    private ResultSet rs;

    private GadgetDao gadgetDao;
    private CustomerDao customerDao;
    private TechnicianDao technicianDao;

    private JdbcRepairOrderDao dao;

    private Gadget gadget;
    private Customer customer;
    private Technician technician;

    @BeforeEach
    void setUp() throws SQLException {
        ConnectionProvider provider = mock(ConnectionProvider.class);
        connection = mock(Connection.class);
        ps = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);

        providerStatic = Mockito.mockStatic(ConnectionProvider.class);
        providerStatic.when(ConnectionProvider::getInstance).thenReturn(provider);
        when(provider.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(ps);

        gadgetDao = mock(GadgetDao.class);
        customerDao = mock(CustomerDao.class);
        technicianDao = mock(TechnicianDao.class);
        dao = new JdbcRepairOrderDao(gadgetDao, customerDao, technicianDao);

        gadget = new GadgetBuilder().id(1).name("iPhone 15").manufacturer("Apple")
                .price(new BigDecimal("999")).type(GadgetType.SMARTPHONE).screen(1179, 2556).build();
        customer = new Customer("Ivanenko", "Ivan", "+380501112233");
        customer.setId(2);
        technician = new Technician("Petrenko", "Petro");
        technician.setId(3);
    }

    @AfterEach
    void tearDown() {
        providerStatic.close();
    }

    private RepairOrderBuilder baseOrder() {
        return new RepairOrderBuilder().gadget(gadget).customer(customer)
                .problem("Cracked screen").createdAt(CREATED);
    }

    // ---------- save ----------

    @Test
    void saveBindsAllFieldsAndSetsGeneratedId() throws SQLException {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(55);
        RepairOrder order = baseOrder().technician(technician).status(RepairStatus.DONE)
                .cost(new BigDecimal("1500.00")).finishedAt(FINISHED).build();

        dao.save(order);

        assertEquals(55, order.getId());
        verify(ps).setInt(1, 1);
        verify(ps).setInt(2, 2);
        verify(ps).setInt(3, 3);
        verify(ps).setString(4, "Cracked screen");
        verify(ps).setString(5, "DONE");
        verify(ps).setBigDecimal(6, new BigDecimal("1500.00"));
        verify(ps).setTimestamp(7, Timestamp.valueOf(CREATED));
        verify(ps).setTimestamp(8, Timestamp.valueOf(FINISHED));
    }

    @Test
    void saveWritesNullsForMissingTechnicianCostAndFinishDate() throws SQLException {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(56);
        RepairOrder order = baseOrder().build();

        dao.save(order);

        verify(ps).setNull(3, Types.INTEGER);
        verify(ps).setString(5, "ACCEPTED");
        verify(ps).setBigDecimal(eq(6), isNull());
        verify(ps).setNull(8, Types.TIMESTAMP);
    }

    @Test
    void saveWrapsSqlException() throws SQLException {
        when(ps.executeQuery()).thenThrow(new SQLException("boom"));
        RepairOrder order = baseOrder().build();

        assertThrows(DaoException.class, () -> dao.save(order));
    }

    // ---------- update / delete ----------

    @Test
    void updateBindsIdAsLastParameter() throws SQLException {
        RepairOrder order = baseOrder().id(8).technician(technician).status(RepairStatus.IN_PROGRESS).build();

        dao.update(order);

        verify(ps).setString(5, "IN_PROGRESS");
        verify(ps).setInt(9, 8);
        verify(ps).executeUpdate();
    }

    @Test
    void updateWrapsSqlException() throws SQLException {
        when(ps.executeUpdate()).thenThrow(new SQLException("boom"));
        RepairOrder order = baseOrder().id(8).build();

        assertThrows(DaoException.class, () -> dao.update(order));
    }

    @Test
    void deleteBindsId() throws SQLException {
        dao.delete(9);

        verify(ps).setInt(1, 9);
        verify(ps).executeUpdate();
    }

    @Test
    void deleteWrapsSqlException() throws SQLException {
        when(ps.executeUpdate()).thenThrow(new SQLException("boom"));

        assertThrows(DaoException.class, () -> dao.delete(9));
    }

    // ---------- mapping ----------

    private void stubOneRow(boolean hasTechnician, Timestamp finishedAt, String status) throws SQLException {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("id")).thenReturn(5);
        when(rs.getInt("gadget_id")).thenReturn(1);
        when(rs.getInt("customer_id")).thenReturn(2);
        when(rs.getInt("technician_id")).thenReturn(hasTechnician ? 3 : 0);
        when(rs.wasNull()).thenReturn(!hasTechnician);
        when(rs.getString("problem")).thenReturn("Cracked screen");
        when(rs.getString("status_code")).thenReturn(status);
        when(rs.getBigDecimal("cost")).thenReturn(new BigDecimal("1500.00"));
        when(rs.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(CREATED));
        when(rs.getTimestamp("finished_at")).thenReturn(finishedAt);
        when(gadgetDao.findById(1)).thenReturn(gadget);
        when(customerDao.findById(2)).thenReturn(customer);
        if (hasTechnician) {
            when(technicianDao.findById(3)).thenReturn(technician);
        }
    }

    @Test
    void findByIdMapsFinishedOrderWithAllRelations() throws SQLException {
        stubOneRow(true, Timestamp.valueOf(FINISHED), "DONE");

        RepairOrder order = dao.findById(5);

        assertEquals(5, order.getId());
        assertSame(gadget, order.getGadget());
        assertSame(customer, order.getCustomer());
        assertSame(technician, order.getTechnician());
        assertEquals("Cracked screen", order.getProblem());
        assertEquals(RepairStatus.DONE, order.getStatus());
        assertEquals(new BigDecimal("1500.00"), order.getCost());
        assertEquals(CREATED, order.getCreatedAt());
        assertEquals(FINISHED, order.getFinishedAt());
        verify(ps).setObject(1, 5);
    }

    @Test
    void findByIdMapsOrderWithoutTechnicianAndFinishDate() throws SQLException {
        stubOneRow(false, null, "ACCEPTED");

        RepairOrder order = dao.findById(5);

        assertNull(order.getTechnician());
        assertNull(order.getFinishedAt());
        assertEquals(RepairStatus.ACCEPTED, order.getStatus());
        verifyNoInteractions(technicianDao);
    }

    @Test
    void findByIdReturnsNullWhenNoRows() throws SQLException {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        assertNull(dao.findById(404));
    }

    @Test
    void findAllWrapsSqlException() throws SQLException {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("boom"));

        assertThrows(DaoException.class, () -> dao.findAll());
    }

    // ---------- filters ----------

    @Test
    void findByStatusFiltersByStatusCode() throws SQLException {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        List<RepairOrder> result = dao.findByStatus(RepairStatus.IN_PROGRESS);

        assertEquals(0, result.size());
        verify(connection).prepareStatement(contains("WHERE status_code = ?"));
        verify(ps).setObject(1, "IN_PROGRESS");
    }

    @Test
    void findByTechnicianCustomerAndGadgetFilterByForeignKeys() throws SQLException {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        dao.findByTechnician(3);
        dao.findByCustomer(2);
        dao.findByGadget(1);

        verify(connection).prepareStatement(contains("WHERE technician_id = ?"));
        verify(connection).prepareStatement(contains("WHERE customer_id = ?"));
        verify(connection).prepareStatement(contains("WHERE gadget_id = ?"));
        verify(ps).setObject(1, 3);
        verify(ps).setObject(1, 2);
        verify(ps).setObject(1, 1);
    }

    @Test
    void findByPeriodUsesCreationDate() throws SQLException {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        dao.findByPeriod(CREATED, FINISHED);

        verify(connection).prepareStatement(contains("created_at BETWEEN ? AND ?"));
        verify(ps).setObject(1, Timestamp.valueOf(CREATED));
        verify(ps).setObject(2, Timestamp.valueOf(FINISHED));
    }

    @Test
    void findFinishedBetweenUsesFinishDateOfDoneOrders() throws SQLException {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        dao.findFinishedBetween(CREATED, FINISHED);

        verify(connection).prepareStatement(contains("status_code = 'DONE' AND finished_at BETWEEN ? AND ?"));
        verify(ps).setObject(1, Timestamp.valueOf(CREATED));
        verify(ps).setObject(2, Timestamp.valueOf(FINISHED));
    }
}
