package sort;

import com.fixmygadget.model.Customer;
import com.fixmygadget.model.Gadget;
import com.fixmygadget.model.GadgetBuilder;
import com.fixmygadget.model.GadgetType;
import com.fixmygadget.model.RepairOrder;
import com.fixmygadget.model.RepairOrderBuilder;
import com.fixmygadget.model.Technician;
import com.fixmygadget.sort.GadgetSortField;
import com.fixmygadget.sort.QuickSort;
import com.fixmygadget.sort.RepairOrderSortField;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SortFieldTest {

    private static Gadget gadget(int id, String name, String price, GadgetType type) {
        return new GadgetBuilder().id(id).name(name).manufacturer("M")
                .price(new BigDecimal(price)).type(type).screen(100, 200).build();
    }

    private static RepairOrder order(int id, Gadget g, String lastName, Technician t) {
        return new RepairOrderBuilder().id(id).gadget(g)
                .customer(new Customer(lastName, "X", "1")).technician(t)
                .problem("p").build();
    }

    private static List<Integer> ids(List<Gadget> gadgets) {
        return gadgets.stream().map(Gadget::getId).toList();
    }

    @Test
    void gadgetByPriceAscendingAndDescending() {
        List<Gadget> data = new ArrayList<>(List.of(
                gadget(1, "A", "300", GadgetType.TABLET),
                gadget(2, "B", "100", GadgetType.WATCH),
                gadget(3, "C", "200", GadgetType.SMARTPHONE)));

        QuickSort.sort(data, GadgetSortField.PRICE.getComparator(true));
        assertEquals(List.of(2, 3, 1), ids(data));

        QuickSort.sort(data, GadgetSortField.PRICE.getComparator(false));
        assertEquals(List.of(1, 3, 2), ids(data));
    }

    @Test
    void gadgetByNameIgnoresCase() {
        List<Gadget> data = new ArrayList<>(List.of(
                gadget(1, "banana", "1", GadgetType.TABLET),
                gadget(2, "Apple", "1", GadgetType.TABLET),
                gadget(3, "cherry", "1", GadgetType.TABLET)));

        QuickSort.sort(data, GadgetSortField.NAME.getComparator(true));

        assertEquals(List.of(2, 1, 3), ids(data));
    }

    @Test
    void equalValuesAreOrderedById() {
        List<Gadget> data = new ArrayList<>(List.of(
                gadget(3, "A", "100", GadgetType.TABLET),
                gadget(1, "B", "100", GadgetType.TABLET),
                gadget(2, "C", "100", GadgetType.TABLET)));

        QuickSort.sort(data, GadgetSortField.PRICE.getComparator(true));

        assertEquals(List.of(1, 2, 3), ids(data));
    }

    @Test
    void orderWithoutTechnicianComesFirst() {
        Gadget g = gadget(1, "G", "1", GadgetType.TABLET);
        List<RepairOrder> data = new ArrayList<>(List.of(
                order(1, g, "Z", new Technician("Petrenko", "P")),
                order(2, g, "Z", null),
                order(3, g, "Z", new Technician("Abramov", "A"))));

        QuickSort.sort(data, RepairOrderSortField.TECHNICIAN.getComparator(true));

        assertEquals(List.of(2, 3, 1), data.stream().map(RepairOrder::getId).toList());
    }

    @Test
    void orderByCostPutsNullFirst() {
        Gadget g = gadget(1, "G", "1", GadgetType.TABLET);
        RepairOrder a = order(1, g, "A", null);
        a.setCost(new BigDecimal("50"));
        RepairOrder b = order(2, g, "A", null);
        RepairOrder c = order(3, g, "A", null);
        c.setCost(new BigDecimal("10"));
        List<RepairOrder> data = new ArrayList<>(List.of(a, b, c));

        QuickSort.sort(data, RepairOrderSortField.COST.getComparator(true));

        assertEquals(List.of(2, 3, 1), data.stream().map(RepairOrder::getId).toList());
    }
}
