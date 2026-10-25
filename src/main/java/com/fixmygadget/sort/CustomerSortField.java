package com.fixmygadget.sort;

import com.fixmygadget.model.Customer;

import java.util.Comparator;

public enum CustomerSortField {
    LAST_NAME("Last name", Comparator.comparing(Customer::getLastName, String.CASE_INSENSITIVE_ORDER)),
    FIRST_NAME("First name", Comparator.comparing(Customer::getFirstName, String.CASE_INSENSITIVE_ORDER)),
    PHONE("Phone", Comparator.comparing(Customer::getPhone));


    private final String label;
    private final Comparator<Customer> comparator;

    CustomerSortField(String label, Comparator<Customer> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    public Comparator<Customer> getComparator(boolean ascending) {
        Comparator<Customer> result = comparator;
        if (!ascending) {
            result = result.reversed();
        }

        //if the comparator compares equal values, then compare by id to ensure natural order
        return result.thenComparing(Customer::getId,
                Comparator.nullsFirst(Comparator.naturalOrder()));
    }

    public String getLabel() { return label; }

    @Override
    public String toString() { return label; }
}