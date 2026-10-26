package sort;

import com.fixmygadget.model.Customer;
import com.fixmygadget.model.Technician;
import com.fixmygadget.sort.CustomerSortField;
import com.fixmygadget.sort.QuickSort;
import com.fixmygadget.sort.TechnicianSortField;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomerAndTechnicianSortFieldTest {

    private static Customer customer(int id, String last, String first, String phone) {
        Customer c = new Customer(last, first, phone);
        c.setId(id);
        return c;
    }

    private static Technician technician(int id, String last, String first) {
        Technician t = new Technician(last, first);
        t.setId(id);
        return t;
    }

    private static List<Integer> customerIds(List<Customer> list) {
        return list.stream().map(Customer::getId).toList();
    }

    private static List<Integer> technicianIds(List<Technician> list) {
        return list.stream().map(Technician::getId).toList();
    }

    @Test
    void customerLastNameIgnoresCase() {
        List<Customer> list = new ArrayList<>(List.of(
                customer(1, "shevchenko", "A", "1"),
                customer(2, "Bondar", "B", "2"),
                customer(3, "KOVAL", "C", "3")));

        QuickSort.sort(list, CustomerSortField.LAST_NAME.getComparator(true));

        assertEquals(List.of(2, 3, 1), customerIds(list));
    }

    @Test
    void customerFirstNameDescending() {
        List<Customer> list = new ArrayList<>(List.of(
                customer(1, "A", "Andrii", "1"),
                customer(2, "B", "Zoryana", "2"),
                customer(3, "C", "Mykola", "3")));

        QuickSort.sort(list, CustomerSortField.FIRST_NAME.getComparator(false));

        assertEquals(List.of(2, 3, 1), customerIds(list));
    }

    @Test
    void customerPhoneAscending() {
        List<Customer> list = new ArrayList<>(List.of(
                customer(1, "A", "A", "+380503"),
                customer(2, "B", "B", "+380501"),
                customer(3, "C", "C", "+380502")));

        QuickSort.sort(list, CustomerSortField.PHONE.getComparator(true));

        assertEquals(List.of(2, 3, 1), customerIds(list));
    }

    @Test
    void customersWithEqualLastNameAreOrderedById() {
        List<Customer> list = new ArrayList<>(List.of(
                customer(3, "Same", "A", "1"),
                customer(1, "Same", "B", "2"),
                customer(2, "Same", "C", "3")));

        QuickSort.sort(list, CustomerSortField.LAST_NAME.getComparator(true));

        assertEquals(List.of(1, 2, 3), customerIds(list));
    }

    @Test
    void technicianLastNameAscending() {
        List<Technician> list = new ArrayList<>(List.of(
                technician(1, "Zaitsev", "A"),
                technician(2, "abramov", "B"),
                technician(3, "Melnyk", "C")));

        QuickSort.sort(list, TechnicianSortField.LAST_NAME.getComparator(true));

        assertEquals(List.of(2, 3, 1), technicianIds(list));
    }

    @Test
    void technicianFirstNameDescending() {
        List<Technician> list = new ArrayList<>(List.of(
                technician(1, "A", "Andrii"),
                technician(2, "B", "Oleh"),
                technician(3, "C", "Ihor")));

        QuickSort.sort(list, TechnicianSortField.FIRST_NAME.getComparator(false));

        assertEquals(List.of(2, 3, 1), technicianIds(list));
    }

    @Test
    void labelsAreReadable() {
        assertEquals("Last name", CustomerSortField.LAST_NAME.getLabel());
        assertEquals("Phone", CustomerSortField.PHONE.toString());
        assertEquals("First name", TechnicianSortField.FIRST_NAME.getLabel());
    }
}
