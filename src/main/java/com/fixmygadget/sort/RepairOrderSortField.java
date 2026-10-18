package com.fixmygadget.sort;

import com.fixmygadget.model.RepairOrder;

import java.util.Comparator;

public enum RepairOrderSortField {
    ID("ID", Comparator.comparing(RepairOrder::getId,
            Comparator.nullsFirst(Comparator.naturalOrder()))),
    //if the comparator compares equal values, then compare by id to ensure natural order

    GADGET("Gadget", Comparator.comparing((RepairOrder o) ->
            o.getGadget().getName(), String.CASE_INSENSITIVE_ORDER)),

    CUSTOMER("Customer", Comparator.comparing((RepairOrder o)
            -> o.getCustomer().getLastName(), String.CASE_INSENSITIVE_ORDER)),

    TECHNICIAN("Technician", Comparator.comparing((RepairOrder o)
            -> o.getTechnician() == null ? "" : o.getTechnician().getLastName(), //if null - first
            String.CASE_INSENSITIVE_ORDER)),

    STATUS("Status", Comparator.comparing(RepairOrder::getStatus)),

    COST("Cost", Comparator.comparing(RepairOrder::getCost,
            Comparator.nullsFirst(Comparator.naturalOrder()))),

    CREATED_AT("Created", Comparator.comparing(RepairOrder::getCreatedAt)),

    FINISHED_AT("Finished", Comparator.comparing(RepairOrder::getFinishedAt,
            Comparator.nullsFirst(Comparator.naturalOrder())));


    private final String label;
    private final Comparator<RepairOrder> comparator;

    RepairOrderSortField(String label, Comparator<RepairOrder> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    public Comparator<RepairOrder> getComparator(boolean ascending) {
        Comparator<RepairOrder> result = comparator;
        if (!ascending) {
            result = result.reversed();
        }

        //if the comparator compares equal values, then compare by id to ensure natural order
        return result.thenComparing(RepairOrder::getId, Comparator.nullsFirst(Comparator.naturalOrder()));
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}