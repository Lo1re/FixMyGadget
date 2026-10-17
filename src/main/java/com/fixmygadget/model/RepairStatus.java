package com.fixmygadget.model;

public enum RepairStatus {
    ACCEPTED("Accepted"), IN_PROGRESS("In progress"), DONE("Done");

    private final String label;

    RepairStatus(String label) { this.label = label; }

    public boolean canMoveTo(RepairStatus next) {
        return switch (this) {
            case ACCEPTED -> next == IN_PROGRESS;
            case IN_PROGRESS -> next == DONE;
            case DONE -> false;
        };
    }

    public String getLabel() { return label; }

    @Override
    public String toString() { return label; }
}