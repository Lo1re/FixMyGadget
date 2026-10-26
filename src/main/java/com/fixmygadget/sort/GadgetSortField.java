package com.fixmygadget.sort;

import com.fixmygadget.model.Gadget;

import java.util.Comparator;
/**
 * Defines the supported sorting fields for gadgets.
 *
 * <p>Encapsulates the comparison logic for gadget attributes, allowing
 * gadgets to be sorted according to different criteria.</p>
 *
 * <p>Implements the Strategy design pattern by providing interchangeable
 * sorting strategies for gadget data.</p>
 */
public enum GadgetSortField {
    NAME("Name", Comparator.comparing(Gadget::getName, String.CASE_INSENSITIVE_ORDER)),
    MANUFACTURER("Manufacturer", Comparator.comparing(Gadget::getManufacturer, String.CASE_INSENSITIVE_ORDER)),
    PRICE("Price", Comparator.comparing(Gadget::getPrice)),
    TYPE("Type", Comparator.comparing(Gadget::getType)),
    SCREEN_WIDTH("Screen width", Comparator.comparingInt(Gadget::getScreenWidth)),
    SCREEN_HEIGHT("Screen height", Comparator.comparingInt(Gadget::getScreenHeight));


    private final String label;
    private final Comparator<Gadget> comparator;

    GadgetSortField(String label, Comparator<Gadget> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    public Comparator<Gadget> getComparator(boolean ascending) {
        Comparator<Gadget> result = comparator;
        if (!ascending) {
            result = result.reversed();
        }

        //if the comparator compares equal values, then compare by id to ensure natural order
        return result.thenComparing(Gadget::getId,
                Comparator.nullsFirst(Comparator.naturalOrder()));
    }

    public String getLabel() { return label; }

    @Override
    public String toString() { return label; }
}
