package service;

import com.fixmygadget.dao.CustomerDao;
import com.fixmygadget.dao.GadgetDao;
import com.fixmygadget.dao.RepairOrderDao;
import com.fixmygadget.dao.TechnicianDao;
import com.fixmygadget.exception.InvalidStatusTransitionException;
import com.fixmygadget.exception.NotFoundException;
import com.fixmygadget.model.Customer;
import com.fixmygadget.model.Gadget;
import com.fixmygadget.model.GadgetBuilder;
import com.fixmygadget.model.GadgetType;
import com.fixmygadget.model.RepairOrder;
import com.fixmygadget.model.RepairOrderBuilder;
import com.fixmygadget.model.RepairStatus;
import com.fixmygadget.model.Technician;
import com.fixmygadget.service.RepairService;
import com.fixmygadget.sort.RepairOrderSortField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepairServiceTest {

    @Mock private RepairOrderDao orderDao;
    @Mock private GadgetDao gadgetDao;
    @Mock private CustomerDao customerDao;
    @Mock private TechnicianDao technicianDao;

    private RepairService service;

    private Gadget gadget;
    private Customer customer;

    @BeforeEach
    void setUp() {
        service = new RepairService(orderDao, gadgetDao, customerDao, technicianDao);
        gadget = new GadgetBuilder().id(1).name("iPhone").manufacturer("Apple")
                .price(new BigDecimal("999")).type(GadgetType.SMARTPHONE).screen(1170, 2532).build();
        customer = new Customer("Ivanenko", "Ivan", "+380501112233");
        customer.setId(2);
    }

    private RepairOrder order(int id, RepairStatus status, BigDecimal cost) {
        return new RepairOrderBuilder().id(id).gadget(gadget).customer(customer)
                .problem("Broken").status(status).cost(cost).build();
    }



    @Test
    void createOrderSavesOrderWithAcceptedStatus() {
        when(gadgetDao.findById(1)).thenReturn(gadget);
        when(customerDao.findById(2)).thenReturn(customer);

        RepairOrder result = service.createOrder(1, 2, "Cracked screen");

        ArgumentCaptor<RepairOrder> captor = ArgumentCaptor.forClass(RepairOrder.class);
        verify(orderDao).save(captor.capture());
        RepairOrder saved = captor.getValue();
        assertSame(saved, result);
        assertSame(gadget, saved.getGadget());
        assertSame(customer, saved.getCustomer());
        assertEquals("Cracked screen", saved.getProblem());
        assertEquals(RepairStatus.ACCEPTED, saved.getStatus());
        assertNotNull(saved.getCreatedAt());
        assertNull(saved.getTechnician());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t\n"})
    void createOrderRejectsBlankProblem(String problem) {
        assertThrows(IllegalArgumentException.class, () -> service.createOrder(1, 2, problem));
        verifyNoInteractions(orderDao, gadgetDao, customerDao);
    }

    @Test
    void createOrderRejectsNullProblem() {
        assertThrows(IllegalArgumentException.class, () -> service.createOrder(1, 2, null));
        verifyNoInteractions(orderDao);
    }

    @Test
    void createOrderFailsWhenGadgetNotFound() {
        when(gadgetDao.findById(1)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> service.createOrder(1, 2, "p"));

        verify(orderDao, never()).save(any());
    }

    @Test
    void createOrderFailsWhenCustomerNotFound() {
        when(gadgetDao.findById(1)).thenReturn(gadget);
        when(customerDao.findById(2)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> service.createOrder(1, 2, "p"));

        verify(orderDao, never()).save(any());
    }


    @Test
    void assignTechnicianUpdatesOrder() {
        RepairOrder order = order(10, RepairStatus.ACCEPTED, null);
        Technician tech = new Technician("Petrenko", "Petro");
        when(orderDao.findById(10)).thenReturn(order);
        when(technicianDao.findById(5)).thenReturn(tech);

        service.assignTechnician(10, 5);

        assertSame(tech, order.getTechnician());
        verify(orderDao).update(order);
    }

    @Test
    void assignTechnicianFailsWhenOrderNotFound() {
        when(orderDao.findById(10)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> service.assignTechnician(10, 5));

        verify(orderDao, never()).update(any());
    }

    @Test
    void assignTechnicianFailsWhenTechnicianNotFound() {
        when(orderDao.findById(10)).thenReturn(order(10, RepairStatus.ACCEPTED, null));
        when(technicianDao.findById(5)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> service.assignTechnician(10, 5));

        verify(orderDao, never()).update(any());
    }


    @Test
    void setCostUpdatesOrder() {
        RepairOrder order = order(10, RepairStatus.ACCEPTED, null);
        when(orderDao.findById(10)).thenReturn(order);

        service.setCost(10, new BigDecimal("120.50"));

        assertEquals(new BigDecimal("120.50"), order.getCost());
        verify(orderDao).update(order);
    }

    @Test
    void setCostAcceptsZero() {
        RepairOrder order = order(10, RepairStatus.ACCEPTED, null);
        when(orderDao.findById(10)).thenReturn(order);

        service.setCost(10, BigDecimal.ZERO);

        assertEquals(BigDecimal.ZERO, order.getCost());
    }

    @Test
    void setCostRejectsNegative() {
        assertThrows(IllegalArgumentException.class, () -> service.setCost(10, new BigDecimal("-1")));
        verifyNoInteractions(orderDao);
    }

    @Test
    void setCostRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> service.setCost(10, null));
        verifyNoInteractions(orderDao);
    }

    @Test
    void changeStatusAcceptedToInProgress() {
        RepairOrder order = order(10, RepairStatus.ACCEPTED, null);
        when(orderDao.findById(10)).thenReturn(order);

        service.changeStatus(10, RepairStatus.IN_PROGRESS);

        assertEquals(RepairStatus.IN_PROGRESS, order.getStatus());
        assertNull(order.getFinishedAt());
        verify(orderDao).update(order);
    }

    @Test
    void changeStatusToDoneSetsFinishedAt() {
        RepairOrder order = order(10, RepairStatus.IN_PROGRESS, new BigDecimal("100"));
        order.setTechnician(new Technician("Petrenko", "Petro"));
        when(orderDao.findById(10)).thenReturn(order);
        LocalDateTime before = LocalDateTime.now();

        service.changeStatus(10, RepairStatus.DONE);

        assertEquals(RepairStatus.DONE, order.getStatus());
        assertNotNull(order.getFinishedAt());
        assertFalse(order.getFinishedAt().isBefore(before));
        verify(orderDao).update(order);
    }

    @Test
    void changeStatusToDoneRequiresTechnician() {
        RepairOrder order = order(10, RepairStatus.IN_PROGRESS, new BigDecimal("100"));
        when(orderDao.findById(10)).thenReturn(order);

        assertThrows(IllegalArgumentException.class, () -> service.changeStatus(10, RepairStatus.DONE));

        assertEquals(RepairStatus.IN_PROGRESS, order.getStatus());
        assertNull(order.getFinishedAt());
        verify(orderDao, never()).update(any());
    }

    @Test
    void assignAndPriceSetsTechnicianAndCost() {
        RepairOrder order = order(10, RepairStatus.ACCEPTED, null);
        Technician tech = new Technician("Petrenko", "Petro");
        when(orderDao.findById(10)).thenReturn(order);
        when(technicianDao.findById(5)).thenReturn(tech);

        service.assignAndPrice(10, 5, new BigDecimal("250"));

        assertSame(tech, order.getTechnician());
        assertEquals(new BigDecimal("250"), order.getCost());
        verify(orderDao).update(order);
    }

    @Test
    void assignAndPriceRequiresTechnicianOrCost() {
        assertThrows(IllegalArgumentException.class, () -> service.assignAndPrice(10, null, null));
        verify(orderDao, never()).update(any());
    }

    @Test
    void changeStatusToDoneRequiresCost() {
        RepairOrder order = order(10, RepairStatus.IN_PROGRESS, null);
        order.setTechnician(new Technician("Petrenko", "Petro"));
        when(orderDao.findById(10)).thenReturn(order);

        assertThrows(IllegalArgumentException.class, () -> service.changeStatus(10, RepairStatus.DONE));

        assertEquals(RepairStatus.IN_PROGRESS, order.getStatus());
        assertNull(order.getFinishedAt());
        verify(orderDao, never()).update(any());
    }

    @Test
    void changeStatusRejectsSkippingStep() {
        RepairOrder order = order(10, RepairStatus.ACCEPTED, new BigDecimal("100"));
        when(orderDao.findById(10)).thenReturn(order);

        assertThrows(InvalidStatusTransitionException.class,
                () -> service.changeStatus(10, RepairStatus.DONE));

        assertEquals(RepairStatus.ACCEPTED, order.getStatus());
        verify(orderDao, never()).update(any());
    }

    @Test
    void changeStatusRejectsMovingBackward() {
        RepairOrder order = order(10, RepairStatus.IN_PROGRESS, null);
        when(orderDao.findById(10)).thenReturn(order);

        assertThrows(InvalidStatusTransitionException.class,
                () -> service.changeStatus(10, RepairStatus.ACCEPTED));
    }

    @Test
    void changeStatusRejectsAnyChangeFromDone() {
        RepairOrder order = order(10, RepairStatus.DONE, new BigDecimal("100"));
        when(orderDao.findById(10)).thenReturn(order);

        assertThrows(InvalidStatusTransitionException.class,
                () -> service.changeStatus(10, RepairStatus.IN_PROGRESS));
        verify(orderDao, never()).update(any());
    }

    @Test
    void changeStatusFailsWhenOrderNotFound() {
        when(orderDao.findById(10)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> service.changeStatus(10, RepairStatus.IN_PROGRESS));
    }


    @Test
    void deleteOrderDeletesExisting() {
        when(orderDao.findById(10)).thenReturn(order(10, RepairStatus.ACCEPTED, null));

        service.deleteOrder(10);

        verify(orderDao).delete(10);
    }

    @Test
    void deleteOrderFailsWhenNotFound() {
        when(orderDao.findById(10)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> service.deleteOrder(10));

        verify(orderDao, never()).delete(any());
    }

    @Test
    void getByIdReturnsOrder() {
        RepairOrder order = order(10, RepairStatus.ACCEPTED, null);
        when(orderDao.findById(10)).thenReturn(order);

        assertSame(order, service.getById(10));
    }

    @Test
    void getByIdFailsWhenMissing() {
        when(orderDao.findById(99)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> service.getById(99));
    }



    @Test
    void getAllDelegatesToDao() {
        List<RepairOrder> orders = List.of(order(1, RepairStatus.ACCEPTED, null));
        when(orderDao.findAll()).thenReturn(orders);

        assertSame(orders, service.getAll());
    }

    @Test
    void findByStatusDelegatesToDao() {
        List<RepairOrder> orders = List.of(order(1, RepairStatus.DONE, BigDecimal.ONE));
        when(orderDao.findByStatus(RepairStatus.DONE)).thenReturn(orders);

        assertSame(orders, service.findByStatus(RepairStatus.DONE));
    }

    @Test
    void findByTechnicianCustomerAndGadgetDelegateToDao() {
        List<RepairOrder> a = List.of(order(1, RepairStatus.ACCEPTED, null));
        List<RepairOrder> b = List.of(order(2, RepairStatus.ACCEPTED, null));
        List<RepairOrder> c = List.of(order(3, RepairStatus.ACCEPTED, null));
        when(orderDao.findByTechnician(5)).thenReturn(a);
        when(orderDao.findByCustomer(2)).thenReturn(b);
        when(orderDao.findByGadget(1)).thenReturn(c);

        assertSame(a, service.findByTechnician(5));
        assertSame(b, service.findByCustomer(2));
        assertSame(c, service.findByGadget(1));
    }

    @Test
    void historyMethodsUseCustomerAndGadgetQueries() {
        List<RepairOrder> byCustomer = List.of(order(1, RepairStatus.ACCEPTED, null));
        List<RepairOrder> byGadget = List.of(order(2, RepairStatus.ACCEPTED, null));
        when(orderDao.findByCustomer(2)).thenReturn(byCustomer);
        when(orderDao.findByGadget(1)).thenReturn(byGadget);

        assertSame(byCustomer, service.getCustomerHistory(2));
        assertSame(byGadget, service.getGadgetHistory(1));
    }

    @Test
    void findByPeriodCoversWholeDays() {
        List<RepairOrder> orders = List.of(order(1, RepairStatus.ACCEPTED, null));
        LocalDate from = LocalDate.of(2025, 3, 1);
        LocalDate to = LocalDate.of(2025, 3, 31);
        when(orderDao.findByPeriod(LocalDateTime.of(2025, 3, 1, 0, 0, 0),
                LocalDateTime.of(2025, 3, 31, 23, 59, 59))).thenReturn(orders);

        assertSame(orders, service.findByPeriod(from, to));
    }


    @Test
    void sortOrdersByCostAscending() {
        List<RepairOrder> orders = new ArrayList<>(List.of(
                order(1, RepairStatus.DONE, new BigDecimal("300")),
                order(2, RepairStatus.DONE, new BigDecimal("100")),
                order(3, RepairStatus.DONE, new BigDecimal("200"))));

        List<RepairOrder> sorted = service.sort(orders, RepairOrderSortField.COST, true);

        assertSame(orders, sorted);
        assertEquals(List.of(2, 3, 1), sorted.stream().map(RepairOrder::getId).toList());
    }

    @Test
    void sortOrdersByIdDescending() {
        List<RepairOrder> orders = new ArrayList<>(List.of(
                order(1, RepairStatus.ACCEPTED, null),
                order(3, RepairStatus.ACCEPTED, null),
                order(2, RepairStatus.ACCEPTED, null)));

        service.sort(orders, RepairOrderSortField.ID, false);

        assertEquals(List.of(3, 2, 1), orders.stream().map(RepairOrder::getId).toList());
    }

    @Test
    void sortEmptyListIsSafe() {
        List<RepairOrder> orders = new ArrayList<>();
        assertEquals(0, service.sort(orders, RepairOrderSortField.STATUS, true).size());
    }
}