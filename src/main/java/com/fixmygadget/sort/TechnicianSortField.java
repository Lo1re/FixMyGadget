package com.fixmygadget.sort;

import com.fixmygadget.model.Technician;

import java.util.Comparator;

public enum TechnicianSortField {
    LAST_NAME("Last name", Comparator.comparing(Technician::getLastName, String.CASE_INSENSITIVE_ORDER)),
    FIRST_NAME("First name", Comparator.comparing(Technician::getFirstName, String.CASE_INSENSITIVE_ORDER));


    private final String label;
    private final Comparator<Technician> comparator;

    TechnicianSortField(String label, Comparator<Technician> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    public Comparator<Technician> getComparator(boolean ascending) {
        Comparator<Technician> result = comparator;
        if (!ascending) {
            result = result.reversed();
        }

        //if the comparator compares equal values, then compare by id to ensure natural order
        return result.thenComparing(Technician::getId,
                Comparator.nullsFirst(Comparator.naturalOrder()));
    }

    public String getLabel() { return label; }

    @Override
    public String toString() { return label; }
}