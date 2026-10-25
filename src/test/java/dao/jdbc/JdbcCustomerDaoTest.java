package dao.jdbc;

import com.fixmygadget.dao.jdbc.JdbcCustomerDao;
import com.fixmygadget.db.ConnectionProvider;
import com.fixmygadget.exception.DaoException;
import com.fixmygadget.model.Customer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class JdbcCustomerDaoTest {

    private MockedStatic<ConnectionProvider> providerStatic;
    private Connection connection;
    private PreparedStatement ps;
    private ResultSet rs;

    private JdbcCustomerDao dao;

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

        dao = new JdbcCustomerDao();
    }

    @AfterEach
    void tearDown() {
        providerStatic.close();
    }

    @Test
    void saveSetsGeneratedId() throws SQLException {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(77);
        Customer c = new Customer("Ivanenko", "Ivan", "+380501112233");

        dao.save(c);

        assertEquals(77, c.getId());
        verify(ps).setString(1, "Ivanenko");
        verify(ps).setString(2, "Ivan");
        verify(ps).setString(3, "+380501112233");
    }

    @Test
    void saveWrapsSqlException() throws SQLException {
        when(ps.executeQuery()).thenThrow(new SQLException("boom"));

        assertThrows(DaoException.class, () -> dao.save(new Customer("A", "B", "1")));
    }

    @Test
    void updateBindsAllParameters() throws SQLException {
        Customer c = new Customer("Ivanenko", "Ivan", "+380501112233");
        c.setId(5);

        dao.update(c);

        verify(ps).setString(1, "Ivanenko");
        verify(ps).setString(2, "Ivan");
        verify(ps).setString(3, "+380501112233");
        verify(ps).setInt(4, 5);
        verify(ps).executeUpdate();
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

    @Test
    void findByIdMapsRow() throws SQLException {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("id")).thenReturn(3);
        when(rs.getString("last_name")).thenReturn("Shevchenko");
        when(rs.getString("first_name")).thenReturn("Taras");
        when(rs.getString("phone")).thenReturn("+380671234567");

        Customer c = dao.findById(3);

        assertEquals(3, c.getId());
        assertEquals("Shevchenko", c.getLastName());
        assertEquals("Taras", c.getFirstName());
        assertEquals("+380671234567", c.getPhone());
        verify(ps).setObject(1, 3);
    }

    @Test
    void findByIdReturnsNullWhenNoRows() throws SQLException {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        assertNull(dao.findById(404));
    }

    @Test
    void findByPhoneReturnsNullWhenNoRows() throws SQLException {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        assertNull(dao.findByPhone("000"));
        verify(ps).setObject(1, "000");
    }

    @Test
    void findAllReturnsAllRows() throws SQLException {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getInt("id")).thenReturn(1, 2);
        when(rs.getString("last_name")).thenReturn("A", "B");
        when(rs.getString("first_name")).thenReturn("X", "Y");
        when(rs.getString("phone")).thenReturn("1", "2");

        List<Customer> all = dao.findAll();

        assertEquals(2, all.size());
        assertEquals("A", all.get(0).getLastName());
        assertEquals("B", all.get(1).getLastName());
    }

    @Test
    void findAllWrapsSqlException() throws SQLException {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("boom"));

        assertThrows(DaoException.class, () -> dao.findAll());
    }
}
