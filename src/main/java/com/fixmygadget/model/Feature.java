package com.fixmygadget.model;
/**
 * Defines the additional features supported by gadgets in the system.
 *
 * <p>Provides predefined feature values for a camera, GPS, and heart
 * rate monitor. Each constant has a display label exposed through
 * {@code getLabel()} and {@code toString()}.</p>
 *
 * <p>Design approach: Enum-based fixed set of domain values.
 * Prevents arbitrary feature values from being used where a
 * predefined gadget feature is expected.</p>
 */
public enum Feature {
    CAMERA("Camera"), GPS("GPS"), HEART_RATE("Heart rate monitor");

    private final String label;

    Feature(String label) { this.label = label; }

    public String getLabel() { return label; }

    @Override
    public String toString() { return label; }
}