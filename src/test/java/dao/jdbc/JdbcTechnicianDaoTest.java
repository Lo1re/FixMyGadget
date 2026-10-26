package dao.jdbc;

import com.fixmygadget.dao.jdbc.JdbcTechnicianDao;
import com.fixmygadget.db.ConnectionProvider;
import com.fixmygadget.exception.DaoException;
import com.fixmygadget.model.Technician;
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

class JdbcTechnicianDaoTest {

    private MockedStatic<ConnectionProvider> providerStatic;
    private Connection connection;
    private PreparedStatement ps;
    private ResultSet rs;

    private JdbcTechnicianDao dao;

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

        dao = new JdbcTechnicianDao();
    }

    @AfterEach
    void tearDown() {
        providerStatic.close();
    }

    @Test
    void saveSetsGeneratedId() throws SQLException {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(12);
        Technician t = new Technician("Petrenko", "Petro");

        dao.save(t);

        assertEquals(12, t.getId());
        verify(ps).setString(1, "Petrenko");
        verify(ps).setString(2, "Petro");
    }

    @Test
    void saveWrapsSqlException() throws SQLException {
        when(ps.executeQuery()).thenThrow(new SQLException("boom"));

        assertThrows(DaoException.class, () -> dao.save(new Technician("A", "B")));
    }

    @Test
    void updateBindsAllParameters() throws SQLException {
        Technician t = new Technician("Petrenko", "Petro");
        t.setId(4);

        dao.update(t);

        verify(ps).setString(1, "Petrenko");
        verify(ps).setString(2, "Petro");
        verify(ps).setInt(3, 4);
        verify(ps).executeUpdate();
    }

    @Test
    void updateWrapsSqlException() throws SQLException {
        when(ps.executeUpdate()).thenThrow(new SQLException("boom"));
        Technician t = new Technician("A", "B");
        t.setId(1);

        assertThrows(DaoException.class, () -> dao.update(t));
    }

    @Test
    void deleteBindsId() throws SQLException {
        dao.delete(6);

        verify(ps).setInt(1, 6);
        verify(ps).executeUpdate();
    }

    @Test
    void deleteWrapsSqlException() throws SQLException {
        when(ps.executeUpdate()).thenThrow(new SQLException("boom"));

        assertThrows(DaoException.class, () -> dao.delete(6));
    }

    @Test
    void findByIdMapsRow() throws SQLException {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("id")).thenReturn(3);
        when(rs.getString("last_name")).thenReturn("Melnyk");
        when(rs.getString("first_name")).thenReturn("Ihor");

        Technician t = dao.findById(3);

        assertEquals(3, t.getId());
        assertEquals("Melnyk", t.getLastName());
        assertEquals("Ihor", t.getFirstName());
        verify(ps).setObject(1, 3);
    }

    @Test
    void findByIdReturnsNullWhenNoRows() throws SQLException {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        assertNull(dao.findById(404));
    }

    @Test
    void findAllReturnsAllRows() throws SQLException {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getInt("id")).thenReturn(1, 2);
        when(rs.getString("last_name")).thenReturn("A", "B");
        when(rs.getString("first_name")).thenReturn("X", "Y");

        List<Technician> all = dao.findAll();

        assertEquals(2, all.size());
        assertEquals("A", all.get(0).getLastName());
        assertEquals("Y", all.get(1).getFirstName());
    }

    @Test
    void findAllWrapsSqlException() throws SQLException {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("boom"));

        assertThrows(DaoException.class, () -> dao.findAll());
    }
}
