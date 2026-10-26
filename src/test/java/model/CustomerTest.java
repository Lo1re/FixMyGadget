package model;

import com.fixmygadget.model.Customer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CustomerTest {

    @Test
    void emptyConstructorLeavesFieldsNull() {
        Customer c = new Customer();

        assertNull(c.getId());
        assertNull(c.getLastName());
        assertNull(c.getFirstName());
        assertNull(c.getPhone());
    }

    @Test
    void fullConstructorSetsFieldsAndLeavesIdEmpty() {
        Customer c = new Customer("Ivanenko", "Ivan", "+380501112233");

        assertNull(c.getId());
        assertEquals("Ivanenko", c.getLastName());
        assertEquals("Ivan", c.getFirstName());
        assertEquals("+380501112233", c.getPhone());
    }

    @Test
    void settersChangeValues() {
        Customer c = new Customer();

        c.setId(7);
        c.setLastName("Shevchenko");
        c.setFirstName("Taras");
        c.setPhone("+380671234567");

        assertEquals(7, c.getId());
        assertEquals("Shevchenko", c.getLastName());
        assertEquals("Taras", c.getFirstName());
        assertEquals("+380671234567", c.getPhone());
    }

    @Test
    void toStringShowsNameAndPhone() {
        Customer c = new Customer("Ivanenko", "Ivan", "+380501112233");

        assertEquals("Ivanenko Ivan (+380501112233)", c.toString());
    }
}
