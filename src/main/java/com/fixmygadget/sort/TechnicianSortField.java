package com.fixmygadget.sort;

import com.fixmygadget.model.Technician;

import java.util.Comparator;
/**
 * Defines the available sorting criteria for technicians.
 *
 * <p>Each enum constant associates a user-friendly label with a comparator
 * that sorts technicians by last name or first name.
 * Name comparisons are case-insensitive.</p>
 *
 * <p>Provides ascending and descending comparators with technician ID as
 * a secondary sorting criterion to ensure deterministic ordering when
 * the primary field values are equal.</p>
 *
 * <p>Implements the Strategy design pattern by encapsulating interchangeable
 * technician sorting strategies.</p>
 */
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