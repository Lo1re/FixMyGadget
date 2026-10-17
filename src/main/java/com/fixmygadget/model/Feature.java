package com.fixmygadget.model;

public enum Feature {
    CAMERA("Camera"), GPS("GPS"), HEART_RATE("Heart rate monitor");

    private final String label;

    Feature(String label) { this.label = label; }

    public String getLabel() { return label; }

    @Override
    public String toString() { return label; }
}