package model;

import com.fixmygadget.model.Customer;
import com.fixmygadget.model.Gadget;
import com.fixmygadget.model.GadgetBuilder;
import com.fixmygadget.model.GadgetType;
import com.fixmygadget.model.RepairOrder;
import com.fixmygadget.model.RepairStatus;
import com.fixmygadget.model.Technician;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepairOrderTest {

    private final Gadget gadget = new GadgetBuilder().id(1).name("iPhone 15").manufacturer("Apple")
            .price(new BigDecimal("999")).type(GadgetType.SMARTPHONE).screen(1179, 2556).build();
    private final Customer customer = new Customer("Ivanenko", "Ivan", "+380501112233");

    @Test
    void newOrderStartsAsAcceptedWithoutTechnicianCostOrFinishDate() {
        RepairOrder order = new RepairOrder(gadget, customer, "Cracked screen");

        assertEquals(RepairStatus.ACCEPTED, order.getStatus());
        assertNull(order.getId());
        assertNull(order.getTechnician());
        assertNull(order.getCost());
        assertNull(order.getFinishedAt());
    }

    @Test
    void constructorSetsMainFieldsAndCreationTime() {
        LocalDateTime before = LocalDateTime.now();

        RepairOrder order = new RepairOrder(gadget, customer, "Cracked screen");

        LocalDateTime after = LocalDateTime.now();
        assertSame(gadget, order.getGadget());
        assertSame(customer, order.getCustomer());
        assertEquals("Cracked screen", order.getProblem());
        assertNotNull(order.getCreatedAt());
        assertTrue(!order.getCreatedAt().isBefore(before) && !order.getCreatedAt().isAfter(after));
    }

    @Test
    void emptyConstructorDefaultsToAcceptedStatus() {
        RepairOrder order = new RepairOrder();

        assertEquals(RepairStatus.ACCEPTED, order.getStatus());
        assertNull(order.getCreatedAt());
    }

    @Test
    void settersChangeValues() {
        RepairOrder order = new RepairOrder();
        Technician technician = new Technician("Petrenko", "Petro");
        LocalDateTime created = LocalDateTime.of(2026, 10, 1, 9, 0);
        LocalDateTime finished = LocalDateTime.of(2026, 10, 3, 17, 30);

        order.setId(12);
        order.setTechnician(technician);
        order.setStatus(RepairStatus.DONE);
        order.setCost(new BigDecimal("1500.00"));
        order.setCreatedAt(created);
        order.setFinishedAt(finished);

        assertEquals(12, order.getId());
        assertSame(technician, order.getTechnician());
        assertEquals(RepairStatus.DONE, order.getStatus());
        assertEquals(new BigDecimal("1500.00"), order.getCost());
        assertEquals(created, order.getCreatedAt());
        assertEquals(finished, order.getFinishedAt());
    }

    @Test
    void toStringContainsIdStatusGadgetAndProblem() {
        RepairOrder order = new RepairOrder(gadget, customer, "Cracked screen");
        order.setId(5);

        String text = order.toString();

        assertTrue(text.contains("#5"));
        assertTrue(text.contains("Accepted"));
        assertTrue(text.contains("iPhone 15"));
        assertTrue(text.contains("Cracked screen"));
    }
}
