package com.fixmygadget.model;
/**
 * Defines the supported categories of gadgets in the application.
 *
 * <p>Provides the predefined types {@code SMARTPHONE}, {@code TABLET},
 * and {@code WATCH}. Each type has a human-readable label available
 * through {@code getLabel()} and {@code toString()}.</p>
 *
 * <p>Design approach: Enum-based domain classification.
 * Restricts gadget types to a fixed set of supported values and
 * provides consistent display labels for the user interface.</p>
 */
public enum GadgetType {
    SMARTPHONE("Smartphone"), TABLET("Tablet"), WATCH("Watch");

    private final String label;

    GadgetType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}