package model;

import com.fixmygadget.model.Technician;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TechnicianTest {

    @Test
    void emptyConstructorLeavesFieldsNull() {
        Technician t = new Technician();

        assertNull(t.getId());
        assertNull(t.getLastName());
        assertNull(t.getFirstName());
    }

    @Test
    void fullConstructorSetsNames() {
        Technician t = new Technician("Petrenko", "Petro");

        assertNull(t.getId());
        assertEquals("Petrenko", t.getLastName());
        assertEquals("Petro", t.getFirstName());
    }

    @Test
    void settersChangeValues() {
        Technician t = new Technician();

        t.setId(3);
        t.setLastName("Melnyk");
        t.setFirstName("Ihor");

        assertEquals(3, t.getId());
        assertEquals("Melnyk", t.getLastName());
        assertEquals("Ihor", t.getFirstName());
    }

    @Test
    void toStringShowsLastAndFirstName() {
        assertEquals("Petrenko Petro", new Technician("Petrenko", "Petro").toString());
    }
}
