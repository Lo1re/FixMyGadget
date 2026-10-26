package dao.jdbc;

import com.fixmygadget.dao.jdbc.JdbcGadgetDao;
import com.fixmygadget.db.ConnectionProvider;
import com.fixmygadget.exception.DaoException;
import com.fixmygadget.model.Feature;
import com.fixmygadget.model.Gadget;
import com.fixmygadget.model.GadgetBuilder;
import com.fixmygadget.model.GadgetType;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcGadgetDaoTest {

    private MockedStatic<ConnectionProvider> providerStatic;
    private Connection connection;
    private PreparedStatement ps;
    private ResultSet rs;

    private JdbcGadgetDao dao;

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

        dao = new JdbcGadgetDao();
    }

    @AfterEach
    void tearDown() {
        providerStatic.close();
    }

    private static Gadget iphone() {
        return new GadgetBuilder().name("iPhone 15").manufacturer("Apple")
                .price(new BigDecimal("999.00")).type(GadgetType.SMARTPHONE)
                .screen(1179, 2556).feature(Feature.CAMERA).build();
    }

    // ---------- save ----------

    @Test
    void saveReusesExistingManufacturerAndSetsGeneratedId() throws SQLException {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(10, 77);   // manufacturer id, then gadget id
        Gadget g = iphone();

        dao.save(g);

        assertEquals(77, g.getId());
        verify(connection).setAutoCommit(false);
        verify(ps).setString(1, "Apple");                       // manufacturer lookup
        verify(ps).setString(1, "iPhone 15");                   // gadget name
        verify(ps).setInt(2, 10);                               // manufacturer id
        verify(ps).setBigDecimal(3, new BigDecimal("999.00"));
        verify(ps).setString(4, "SMARTPHONE");
        verify(ps).setInt(5, 1179);
        verify(ps).setInt(6, 2556);
        verify(ps).setInt(1, 77);                               // feature row: gadget id
        verify(ps).setString(2, "CAMERA");                      // feature row: feature code
        verify(connection).commit();
        verify(connection, never()).rollback();
    }

    @Test
    void saveCreatesManufacturerWhenItDoesNotExist() throws SQLException {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false, true, true);   // lookup empty, insert returns id, gadget insert returns id
        when(rs.getInt(1)).thenReturn(10, 77);
        Gadget g = iphone();

        dao.save(g);

        assertEquals(77, g.getId());
        verify(ps, times(2)).setString(1, "Apple");              // lookup + insert
        verify(connection).prepareStatement(contains("INSERT INTO manufacturer"));
        verify(connection).commit();
    }

    @Test
    void saveRollsBackAndWrapsSqlException() throws SQLException {
        when(ps.executeQuery()).thenThrow(new SQLException("boom"));

        assertThrows(DaoException.class, () -> dao.save(iphone()));

        verify(connection).rollback();
        verify(connection, never()).commit();
    }

    // ---------- update ----------

    @Test
    void updateReplacesGadgetAndItsFeaturesInOneTransaction() throws SQLException {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(10);
        Gadget g = iphone();
        g.setId(5);

        dao.update(g);

        verify(connection).setAutoCommit(false);
        verify(ps).setInt(7, 5);                                 // WHERE id = ?
        verify(connection).prepareStatement("DELETE FROM gadget_feature WHERE gadget_id = ?");
        verify(connection).prepareStatement(contains("INSERT INTO gadget_feature"));
        verify(ps).setString(2, "CAMERA");
        verify(connection).commit();
        verify(connection, never()).rollback();
    }

    @Test
    void updateRollsBackAndWrapsSqlException() throws SQLException {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(10);
        when(ps.executeUpdate()).thenThrow(new SQLException("boom"));
        Gadget g = iphone();
        g.setId(5);

        assertThrows(DaoException.class, () -> dao.update(g));

        verify(connection).rollback();
        verify(connection, never()).commit();
    }

    // ---------- delete ----------

    @Test
    void deleteBindsId() throws SQLException {
        dao.delete(9);

        verify(connection).prepareStatement("DELETE FROM gadget WHERE id = ?");
        verify(ps).setInt(1, 9);
        verify(ps).executeUpdate();
    }

    @Test
    void deleteWrapsSqlException() throws SQLException {
        when(ps.executeUpdate()).thenThrow(new SQLException("boom"));

        assertThrows(DaoException.class, () -> dao.delete(9));
    }

    // ---------- find ----------

    @Test
    void findByIdMapsRowAndLoadsFeatures() throws SQLException {
        when(ps.executeQuery()).thenReturn(rs);
        // 1st loop: one gadget row, then end. 2nd loop (features): one row, then end.
        when(rs.next()).thenReturn(true, false, true, false);
        when(rs.getInt("id")).thenReturn(3);
        when(rs.getString("name")).thenReturn("Pixel 8");
        when(rs.getString("manufacturer")).thenReturn("Google");
        when(rs.getBigDecimal("price")).thenReturn(new BigDecimal("699.00"));
        when(rs.getString("type_code")).thenReturn("SMARTPHONE");
        when(rs.getInt("screen_width")).thenReturn(1080);
        when(rs.getInt("screen_height")).thenReturn(2400);
        when(rs.getString("feature_code")).thenReturn("GPS");

        Gadget g = dao.findById(3);

        assertEquals(3, g.getId());
        assertEquals("Pixel 8", g.getName());
        assertEquals("Google", g.getManufacturer());
        assertEquals(new BigDecimal("699.00"), g.getPrice());
        assertEquals(GadgetType.SMARTPHONE, g.getType());
        assertEquals(1080, g.getScreenWidth());
        assertEquals(2400, g.getScreenHeight());
        assertTrue(g.hasFeature(Feature.GPS));
        assertEquals(1, g.getFeatures().size());
        verify(ps).setObject(1, 3);
    }

    @Test
    void findByIdReturnsNullWhenNoRows() throws SQLException {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        assertNull(dao.findById(404));
    }

    @Test
    void findAllReturnsEmptyListWhenNoRows() throws SQLException {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        assertTrue(dao.findAll().isEmpty());
    }

    @Test
    void findAllWrapsSqlException() throws SQLException {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("boom"));

        assertThrows(DaoException.class, () -> dao.findAll());
    }

    @Test
    void findByManufacturerFiltersByManufacturerName() throws SQLException {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        List<Gadget> result = dao.findByManufacturer("Apple");

        assertTrue(result.isEmpty());
        verify(connection).prepareStatement(contains("WHERE m.name = ?"));
        verify(ps).setObject(1, "Apple");
    }

    @Test
    void findByTypeFiltersByTypeCode() throws SQLException {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        dao.findByType(GadgetType.WATCH);

        verify(connection).prepareStatement(contains("g.type_code = ?"));
        verify(ps).setObject(1, "WATCH");
    }

    @Test
    void findByNameAndTypeSearchesCaseInsensitiveSubstring() throws SQLException {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        dao.findByNameAndType("PiXel", GadgetType.SMARTPHONE);

        verify(connection).prepareStatement(contains("LOWER(g.name) LIKE ?"));
        verify(ps).setObject(1, "%pixel%");
        verify(ps).setObject(2, "SMARTPHONE");
    }

    @Test
    void findByScreenSizeBindsWidthRangeThenHeightRange() throws SQLException {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        dao.findByScreenSize(100, 200, 300, 400);   // x1, y1, x2, y2

        verify(ps).setObject(1, 100);   // width from
        verify(ps).setObject(2, 300);   // width to
        verify(ps).setObject(3, 200);   // height from
        verify(ps).setObject(4, 400);   // height to
    }
}
