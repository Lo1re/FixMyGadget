package service;

import com.fixmygadget.dao.RepairOrderDao;
import com.fixmygadget.model.RepairOrder;
import com.fixmygadget.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private RepairOrderDao orderDao;

    private ReportService service;

    @BeforeEach
    void setUp() {
        service = new ReportService(orderDao);
    }

    private static RepairOrder orderWithCost(String cost) {
        RepairOrder order = new RepairOrder();
        order.setCost(new BigDecimal(cost));
        return order;
    }

    @Test
    void revenueIsSumOfFinishedOrderCosts() {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);
        when(orderDao.findFinishedBetween(from.atStartOfDay(), to.atTime(23, 59, 59)))
                .thenReturn(List.of(orderWithCost("100.50"), orderWithCost("200"), orderWithCost("50.25")));

        BigDecimal revenue = service.getRevenue(from, to);

        assertEquals(0, new BigDecimal("350.75").compareTo(revenue));
    }

    @Test
    void revenueIsZeroWhenNothingWasFinished() {
        LocalDate day = LocalDate.of(2026, 10, 1);
        when(orderDao.findFinishedBetween(day.atStartOfDay(), day.atTime(23, 59, 59)))
                .thenReturn(List.of());

        assertEquals(0, BigDecimal.ZERO.compareTo(service.getRevenue(day, day)));
    }

    @Test
    void revenueCoversWholeLastDayOfPeriod() {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 5);
        when(orderDao.findFinishedBetween(LocalDateTime.of(2026, 10, 1, 0, 0, 0),
                LocalDateTime.of(2026, 10, 5, 23, 59, 59))).thenReturn(List.of(orderWithCost("10")));

        service.getRevenue(from, to);

        verify(orderDao).findFinishedBetween(LocalDateTime.of(2026, 10, 1, 0, 0, 0),
                LocalDateTime.of(2026, 10, 5, 23, 59, 59));
    }

    @Test
    void revenueRejectsStartAfterEnd() {
        LocalDate from = LocalDate.of(2026, 10, 10);
        LocalDate to = LocalDate.of(2026, 10, 1);

        assertThrows(IllegalArgumentException.class, () -> service.getRevenue(from, to));

        verifyNoInteractions(orderDao);
    }

    @Test
    void getOrdersByTechnicianDelegatesToDao() {
        List<RepairOrder> orders = List.of(new RepairOrder(), new RepairOrder());
        when(orderDao.findByTechnician(3)).thenReturn(orders);

        assertSame(orders, service.getOrdersByTechnician(3));
    }

    @Test
    void countOrdersByTechnicianReturnsListSize() {
        when(orderDao.findByTechnician(3)).thenReturn(List.of(new RepairOrder(), new RepairOrder(), new RepairOrder()));

        assertEquals(3, service.countOrdersByTechnician(3));
    }

    @Test
    void countOrdersByTechnicianIsZeroWhenNoOrders() {
        when(orderDao.findByTechnician(9)).thenReturn(List.of());

        assertEquals(0, service.countOrdersByTechnician(9));
    }
}
