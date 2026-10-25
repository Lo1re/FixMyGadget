package model;

import com.fixmygadget.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RepairOrderBuilderTest {

    private final Gadget gadget = new GadgetBuilder().name("N").manufacturer("M")
            .price(BigDecimal.TEN).type(GadgetType.TABLET).screen(1, 1).build();
    private final Customer customer = new Customer("Ivanenko", "Ivan", "+380501112233");

    @Test
    void buildsWithDefaults() {
        RepairOrder o = new RepairOrderBuilder()
                .gadget(gadget).customer(customer).problem("Broken screen").build();

        assertSame(gadget, o.getGadget());
        assertSame(customer, o.getCustomer());
        assertEquals("Broken screen", o.getProblem());
        assertEquals(RepairStatus.ACCEPTED, o.getStatus());
        assertNotNull(o.getCreatedAt());
        assertNull(o.getId());
        assertNull(o.getTechnician());
        assertNull(o.getCost());
        assertNull(o.getFinishedAt());
    }

    @Test
    void buildsWithAllFields() {
        Technician t = new Technician("Petrenko", "Petro");
        LocalDateTime created = LocalDateTime.of(2025, 1, 10, 9, 0);
        LocalDateTime finished = LocalDateTime.of(2025, 1, 12, 17, 30);

        RepairOrder o = new RepairOrderBuilder()
                .id(5).gadget(gadget).customer(customer).technician(t)
                .problem("Battery").status(RepairStatus.DONE)
                .cost(new BigDecimal("250.00")).createdAt(created).finishedAt(finished)
                .build();

        assertEquals(5, o.getId());
        assertSame(t, o.getTechnician());
        assertEquals(RepairStatus.DONE, o.getStatus());
        assertEquals(new BigDecimal("250.00"), o.getCost());
        assertEquals(created, o.getCreatedAt());
        assertEquals(finished, o.getFinishedAt());
    }

    @Test
    void missingGadgetFails() {
        assertThrows(IllegalStateException.class,
                () -> new RepairOrderBuilder().customer(customer).problem("p").build());
    }

    @Test
    void missingCustomerFails() {
        assertThrows(IllegalStateException.class,
                () -> new RepairOrderBuilder().gadget(gadget).problem("p").build());
    }

    @Test
    void missingProblemFails() {
        assertThrows(IllegalStateException.class,
                () -> new RepairOrderBuilder().gadget(gadget).customer(customer).build());
    }
}
