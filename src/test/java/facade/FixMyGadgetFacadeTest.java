package facade;

import com.fixmygadget.facade.FixMyGadgetFacade;
import com.fixmygadget.model.Customer;
import com.fixmygadget.model.Feature;
import com.fixmygadget.model.Gadget;
import com.fixmygadget.model.GadgetType;
import com.fixmygadget.model.RepairOrder;
import com.fixmygadget.model.RepairStatus;
import com.fixmygadget.model.Technician;
import com.fixmygadget.service.CustomerService;
import com.fixmygadget.service.GadgetService;
import com.fixmygadget.service.ReportService;
import com.fixmygadget.service.RepairService;
import com.fixmygadget.sort.CustomerSortField;
import com.fixmygadget.sort.GadgetSortField;
import com.fixmygadget.sort.RepairOrderSortField;
import com.fixmygadget.sort.TechnicianSortField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The facade only forwards calls, so every test checks that the call reaches
 * the right service with the same arguments and that the result is passed back unchanged.
 */
@ExtendWith(MockitoExtension.class)
class FixMyGadgetFacadeTest {

    @Mock private GadgetService gadgetService;
    @Mock private CustomerService customerService;
    @Mock private RepairService repairService;
    @Mock private ReportService reportService;

    private FixMyGadgetFacade facade;

    private final Gadget gadget = new Gadget();
    private final Customer customer = new Customer("Ivanenko", "Ivan", "+380501112233");
    private final Technician technician = new Technician("Petrenko", "Petro");
    private final RepairOrder order = new RepairOrder();

    @BeforeEach
    void setUp() {
        facade = new FixMyGadgetFacade(gadgetService, customerService, repairService, reportService);
    }

    // ---------- gadgets ----------

    @Test
    void gadgetCrudIsForwarded() {
        facade.addGadget(gadget);
        facade.updateGadget(gadget);
        facade.deleteGadget(4);

        verify(gadgetService).addGadget(gadget);
        verify(gadgetService).updateGadget(gadget);
        verify(gadgetService).deleteGadget(4);
    }

    @Test
    void getAllGadgetsReturnsServiceResult() {
        List<Gadget> all = List.of(gadget);
        when(gadgetService.getAll()).thenReturn(all);

        assertSame(all, facade.getAllGadgets());
    }

    @Test
    void gadgetSearchesAreForwarded() {
        List<Gadget> result = List.of(gadget);
        when(gadgetService.findByNameAndType("pixel", GadgetType.SMARTPHONE)).thenReturn(result);
        when(gadgetService.findByManufacturer("Apple")).thenReturn(result);
        when(gadgetService.findByType(GadgetType.WATCH)).thenReturn(result);
        when(gadgetService.findByScreenSize(1, 2, 3, 4)).thenReturn(result);

        assertSame(result, facade.findGadgets("pixel", GadgetType.SMARTPHONE));
        assertSame(result, facade.findGadgetsByManufacturer("Apple"));
        assertSame(result, facade.findGadgetsByType(GadgetType.WATCH));
        assertSame(result, facade.findGadgetsByScreenSize(1, 2, 3, 4));
    }

    @Test
    void featureChangesAreForwarded() {
        facade.addFeature(1, Feature.GPS);
        facade.removeFeature(1, Feature.CAMERA);

        verify(gadgetService).addFeature(1, Feature.GPS);
        verify(gadgetService).removeFeature(1, Feature.CAMERA);
    }

    @Test
    void sortGadgetsReturnsServiceResult() {
        List<Gadget> input = List.of(gadget);
        List<Gadget> sorted = List.of(gadget);
        when(gadgetService.sort(input, GadgetSortField.PRICE, false)).thenReturn(sorted);

        assertSame(sorted, facade.sortGadgets(input, GadgetSortField.PRICE, false));
    }

    @Test
    void exportCatalogIsForwarded() throws IOException {
        StringWriter writer = new StringWriter();

        facade.exportCatalog(writer);

        verify(gadgetService).exportCatalog(writer);
    }

    @Test
    void importCatalogReturnsNumberOfImportedGadgets() throws IOException {
        StringReader reader = new StringReader("");
        when(gadgetService.importCatalog(reader)).thenReturn(7);

        assertEquals(7, facade.importCatalog(reader));
    }

    // ---------- customers ----------

    @Test
    void customerCrudIsForwarded() {
        facade.addCustomer(customer);
        facade.updateCustomer(customer);
        facade.deleteCustomer(2);

        verify(customerService).addCustomer(customer);
        verify(customerService).updateCustomer(customer);
        verify(customerService).deleteCustomer(2);
    }

    @Test
    void getAllCustomersReturnsServiceResult() {
        List<Customer> all = List.of(customer);
        when(customerService.getAllCustomers()).thenReturn(all);

        assertSame(all, facade.getAllCustomers());
    }

    @Test
    void sortCustomersReturnsServiceResult() {
        List<Customer> input = List.of(customer);
        List<Customer> sorted = List.of(customer);
        when(customerService.sortCustomers(input, CustomerSortField.PHONE, true)).thenReturn(sorted);

        assertSame(sorted, facade.sortCustomers(input, CustomerSortField.PHONE, true));
    }

    // ---------- technicians ----------

    @Test
    void technicianCrudIsForwardedToCustomerService() {
        facade.addTechnician(technician);
        facade.updateTechnician(technician);
        facade.deleteTechnician(3);

        verify(customerService).addTechnician(technician);
        verify(customerService).updateTechnician(technician);
        verify(customerService).deleteTechnician(3);
    }

    @Test
    void getAllTechniciansReturnsServiceResult() {
        List<Technician> all = List.of(technician);
        when(customerService.getAllTechnicians()).thenReturn(all);

        assertSame(all, facade.getAllTechnicians());
    }

    @Test
    void sortTechniciansReturnsServiceResult() {
        List<Technician> input = List.of(technician);
        List<Technician> sorted = List.of(technician);
        when(customerService.sortTechnicians(input, TechnicianSortField.LAST_NAME, true)).thenReturn(sorted);

        assertSame(sorted, facade.sortTechnicians(input, TechnicianSortField.LAST_NAME, true));
    }

    // ---------- repair orders ----------

    @Test
    void createOrderReturnsCreatedOrder() {
        when(repairService.createOrder(1, 2, "Broken screen")).thenReturn(order);

        assertSame(order, facade.createOrder(1, 2, "Broken screen"));
    }

    @Test
    void orderChangesAreForwarded() {
        BigDecimal cost = new BigDecimal("450.00");

        facade.assignTechnician(10, 3);
        facade.assignAndPrice(10, 3, cost);
        facade.setCost(10, cost);
        facade.changeStatus(10, RepairStatus.IN_PROGRESS);
        facade.deleteOrder(10);

        verify(repairService).assignTechnician(10, 3);
        verify(repairService).assignAndPrice(10, 3, cost);
        verify(repairService).setCost(10, cost);
        verify(repairService).changeStatus(10, RepairStatus.IN_PROGRESS);
        verify(repairService).deleteOrder(10);
    }

    @Test
    void orderQueriesReturnServiceResults() {
        List<RepairOrder> orders = List.of(order);
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);
        when(repairService.getAll()).thenReturn(orders);
        when(repairService.findByStatus(RepairStatus.DONE)).thenReturn(orders);
        when(repairService.findByTechnician(3)).thenReturn(orders);
        when(repairService.findByCustomer(2)).thenReturn(orders);
        when(repairService.findByGadget(1)).thenReturn(orders);
        when(repairService.findByPeriod(from, to)).thenReturn(orders);

        assertSame(orders, facade.getAllOrders());
        assertSame(orders, facade.findOrdersByStatus(RepairStatus.DONE));
        assertSame(orders, facade.findOrdersByTechnician(3));
        assertSame(orders, facade.findOrdersByCustomer(2));
        assertSame(orders, facade.findOrdersByGadget(1));
        assertSame(orders, facade.findOrdersByPeriod(from, to));
    }

    @Test
    void historyIsForwarded() {
        List<RepairOrder> orders = List.of(order);
        when(repairService.getCustomerHistory(2)).thenReturn(orders);
        when(repairService.getGadgetHistory(1)).thenReturn(orders);

        assertSame(orders, facade.getCustomerHistory(2));
        assertSame(orders, facade.getGadgetHistory(1));
    }

    @Test
    void sortOrdersReturnsServiceResult() {
        List<RepairOrder> input = List.of(order);
        List<RepairOrder> sorted = List.of(order);
        when(repairService.sort(input, RepairOrderSortField.COST, false)).thenReturn(sorted);

        assertSame(sorted, facade.sortOrders(input, RepairOrderSortField.COST, false));
    }

    // ---------- reports ----------

    @Test
    void reportsAreForwardedToReportService() {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);
        List<RepairOrder> orders = List.of(order);
        when(reportService.getRevenue(from, to)).thenReturn(new BigDecimal("1234.50"));
        when(reportService.getOrdersByTechnician(3)).thenReturn(orders);
        when(reportService.countOrdersByTechnician(3)).thenReturn(5);

        assertEquals(new BigDecimal("1234.50"), facade.getRevenue(from, to));
        assertSame(orders, facade.getOrdersByTechnician(3));
        assertEquals(5, facade.countOrdersByTechnician(3));
    }
}
