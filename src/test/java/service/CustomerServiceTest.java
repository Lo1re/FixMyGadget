package service;

import com.fixmygadget.dao.CustomerDao;
import com.fixmygadget.dao.TechnicianDao;
import com.fixmygadget.exception.NotFoundException;
import com.fixmygadget.model.Customer;
import com.fixmygadget.model.Technician;
import com.fixmygadget.service.CustomerService;
import com.fixmygadget.sort.CustomerSortField;
import com.fixmygadget.sort.TechnicianSortField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    private static final String PHONE = "+380501112233";

    @Mock private CustomerDao customerDao;
    @Mock private TechnicianDao technicianDao;

    private CustomerService service;

    @BeforeEach
    void setUp() {
        service = new CustomerService(customerDao, technicianDao);
    }

    private static Customer customer(Integer id, String last, String first, String phone) {
        Customer c = new Customer(last, first, phone);
        c.setId(id);
        return c;
    }

    private static Technician technician(Integer id, String last, String first) {
        Technician t = new Technician(last, first);
        t.setId(id);
        return t;
    }

    // ---------- customers: add ----------

    @Test
    void addCustomerSavesValidCustomer() {
        Customer c = customer(null, "Ivanenko", "Ivan", PHONE);
        when(customerDao.findByPhone(PHONE)).thenReturn(null);

        service.addCustomer(c);

        verify(customerDao).save(c);
    }

    @ParameterizedTest
    @ValueSource(strings = {"O'Brien", "Anna-Maria", "Mary Ann", "Іваненко", "Олена-Марія"})
    void addCustomerAcceptsRealWorldNames(String name) {
        Customer c = customer(null, name, "Ivan", PHONE);
        when(customerDao.findByPhone(PHONE)).thenReturn(null);

        service.addCustomer(c);

        verify(customerDao).save(c);
    }

    @Test
    void addCustomerRejectsDuplicatePhone() {
        Customer c = customer(null, "Ivanenko", "Ivan", PHONE);
        when(customerDao.findByPhone(PHONE)).thenReturn(customer(9, "Other", "Person", PHONE));

        assertThrows(IllegalArgumentException.class, () -> service.addCustomer(c));

        verify(customerDao, never()).save(c);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "Ivan3", "Iv@n", "123", "-Ivan"})
    void addCustomerRejectsInvalidLastName(String lastName) {
        Customer c = customer(null, lastName, "Ivan", PHONE);

        assertThrows(IllegalArgumentException.class, () -> service.addCustomer(c));

        verifyNoInteractions(customerDao);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "Ivan3", "Iv@n"})
    void addCustomerRejectsInvalidFirstName(String firstName) {
        Customer c = customer(null, "Ivanenko", firstName, PHONE);

        assertThrows(IllegalArgumentException.class, () -> service.addCustomer(c));

        verifyNoInteractions(customerDao);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "0501112233", "380501112233", "+38050111223",
            "+3805011122334", "+380abcdefghi", "+380 50 111 22 33", "+381501112233"})
    void addCustomerRejectsInvalidPhone(String phone) {
        Customer c = customer(null, "Ivanenko", "Ivan", phone);

        assertThrows(IllegalArgumentException.class, () -> service.addCustomer(c));

        verifyNoInteractions(customerDao);
    }

    // ---------- customers: update ----------

    @Test
    void updateCustomerRejectsMissingId() {
        Customer c = customer(null, "Ivanenko", "Ivan", PHONE);

        assertThrows(IllegalArgumentException.class, () -> service.updateCustomer(c));

        verifyNoInteractions(customerDao);
    }

    @Test
    void updateCustomerRejectsInvalidData() {
        Customer c = customer(1, "Ivanenko", "Ivan", "bad-phone");

        assertThrows(IllegalArgumentException.class, () -> service.updateCustomer(c));

        verifyNoInteractions(customerDao);
    }

    @Test
    void updateCustomerThrowsWhenCustomerDoesNotExist() {
        Customer c = customer(1, "Ivanenko", "Ivan", PHONE);
        when(customerDao.findById(1)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> service.updateCustomer(c));

        verify(customerDao, never()).update(c);
    }

    @Test
    void updateCustomerRejectsPhoneUsedByAnotherCustomer() {
        Customer c = customer(1, "Ivanenko", "Ivan", PHONE);
        when(customerDao.findById(1)).thenReturn(c);
        when(customerDao.findByPhone(PHONE)).thenReturn(customer(2, "Other", "Person", PHONE));

        assertThrows(IllegalArgumentException.class, () -> service.updateCustomer(c));

        verify(customerDao, never()).update(c);
    }

    @Test
    void updateCustomerAllowsKeepingOwnPhone() {
        Customer c = customer(1, "Ivanenko", "Ivan", PHONE);
        when(customerDao.findById(1)).thenReturn(c);
        when(customerDao.findByPhone(PHONE)).thenReturn(customer(1, "Ivanenko", "Ivan", PHONE));

        service.updateCustomer(c);

        verify(customerDao).update(c);
    }

    @Test
    void updateCustomerAllowsFreePhone() {
        Customer c = customer(1, "Ivanenko", "Ivan", PHONE);
        when(customerDao.findById(1)).thenReturn(c);
        when(customerDao.findByPhone(PHONE)).thenReturn(null);

        service.updateCustomer(c);

        verify(customerDao).update(c);
    }

    // ---------- customers: delete / get / list / sort ----------

    @Test
    void deleteCustomerDeletesExistingCustomer() {
        when(customerDao.findById(5)).thenReturn(customer(5, "Ivanenko", "Ivan", PHONE));

        service.deleteCustomer(5);

        verify(customerDao).delete(5);
    }

    @Test
    void deleteCustomerThrowsWhenMissing() {
        when(customerDao.findById(5)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> service.deleteCustomer(5));

        verify(customerDao, never()).delete(5);
    }

    @Test
    void getCustomerByIdReturnsCustomer() {
        Customer c = customer(3, "Ivanenko", "Ivan", PHONE);
        when(customerDao.findById(3)).thenReturn(c);

        assertSame(c, service.getCustomerById(3));
    }

    @Test
    void getCustomerByIdThrowsWhenMissing() {
        when(customerDao.findById(404)).thenReturn(null);

        NotFoundException ex = assertThrows(NotFoundException.class, () -> service.getCustomerById(404));

        assertEquals("Customer not found: id=404", ex.getMessage());
    }

    @Test
    void getAllCustomersReturnsDaoResult() {
        List<Customer> all = List.of(customer(1, "A", "B", "+380000000001"));
        when(customerDao.findAll()).thenReturn(all);

        assertSame(all, service.getAllCustomers());
    }

    @Test
    void sortCustomersByLastNameAscendingAndDescending() {
        List<Customer> list = new ArrayList<>(List.of(
                customer(1, "Shevchenko", "Taras", "+380000000001"),
                customer(2, "bondar", "Olena", "+380000000002"),
                customer(3, "Kovalenko", "Petro", "+380000000003")));

        service.sortCustomers(list, CustomerSortField.LAST_NAME, true);
        assertEquals(List.of(2, 3, 1), idsOf(list));

        service.sortCustomers(list, CustomerSortField.LAST_NAME, false);
        assertEquals(List.of(1, 3, 2), idsOf(list));
    }

    @Test
    void sortCustomersByPhone() {
        List<Customer> list = new ArrayList<>(List.of(
                customer(1, "A", "A", "+380000000003"),
                customer(2, "B", "B", "+380000000001"),
                customer(3, "C", "C", "+380000000002")));

        service.sortCustomers(list, CustomerSortField.PHONE, true);

        assertEquals(List.of(2, 3, 1), idsOf(list));
    }

    // ---------- technicians ----------

    @Test
    void addTechnicianSavesValidTechnician() {
        Technician t = technician(null, "Petrenko", "Petro");

        service.addTechnician(t);

        verify(technicianDao).save(t);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "Petro1", "P_tro"})
    void addTechnicianRejectsInvalidLastName(String lastName) {
        Technician t = technician(null, lastName, "Petro");

        assertThrows(IllegalArgumentException.class, () -> service.addTechnician(t));

        verifyNoInteractions(technicianDao);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "Petro1", "P_tro"})
    void addTechnicianRejectsInvalidFirstName(String firstName) {
        Technician t = technician(null, "Petrenko", firstName);

        assertThrows(IllegalArgumentException.class, () -> service.addTechnician(t));

        verifyNoInteractions(technicianDao);
    }

    @Test
    void updateTechnicianRejectsMissingId() {
        Technician t = technician(null, "Petrenko", "Petro");

        assertThrows(IllegalArgumentException.class, () -> service.updateTechnician(t));

        verifyNoInteractions(technicianDao);
    }

    @Test
    void updateTechnicianThrowsWhenMissing() {
        Technician t = technician(1, "Petrenko", "Petro");
        when(technicianDao.findById(1)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> service.updateTechnician(t));

        verify(technicianDao, never()).update(t);
    }

    @Test
    void updateTechnicianUpdatesExisting() {
        Technician t = technician(1, "Petrenko", "Petro");
        when(technicianDao.findById(1)).thenReturn(t);

        service.updateTechnician(t);

        verify(technicianDao).update(t);
    }

    @Test
    void deleteTechnicianDeletesExisting() {
        when(technicianDao.findById(4)).thenReturn(technician(4, "Petrenko", "Petro"));

        service.deleteTechnician(4);

        verify(technicianDao).delete(4);
    }

    @Test
    void deleteTechnicianThrowsWhenMissing() {
        when(technicianDao.findById(4)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> service.deleteTechnician(4));

        verify(technicianDao, never()).delete(4);
    }

    @Test
    void getTechnicianByIdThrowsWhenMissing() {
        when(technicianDao.findById(8)).thenReturn(null);

        NotFoundException ex = assertThrows(NotFoundException.class, () -> service.getTechnicianById(8));

        assertEquals("Technician not found: id=8", ex.getMessage());
    }

    @Test
    void getAllTechniciansReturnsDaoResult() {
        List<Technician> all = List.of(technician(1, "A", "B"));
        when(technicianDao.findAll()).thenReturn(all);

        assertSame(all, service.getAllTechnicians());
    }

    @Test
    void sortTechniciansByLastNameDescending() {
        List<Technician> list = new ArrayList<>(List.of(
                technician(1, "Abramov", "Oleh"),
                technician(2, "Zaitsev", "Andrii"),
                technician(3, "Melnyk", "Ihor")));

        service.sortTechnicians(list, TechnicianSortField.LAST_NAME, false);

        assertEquals(List.of(2, 3, 1), list.stream().map(Technician::getId).toList());
    }

    private static List<Integer> idsOf(List<Customer> customers) {
        return customers.stream().map(Customer::getId).toList();
    }
}
