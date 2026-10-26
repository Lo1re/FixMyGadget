package com.fixmygadget.model;
/**
 * Defines the available statuses of a repair order and the allowed
 * transitions between them.
 *
 * <p>Provides the statuses {@code ACCEPTED}, {@code IN_PROGRESS},
 * and {@code DONE}, each with a human-readable display label.</p>
 *
 * <p>The {@code canMoveTo()} method determines whether a transition
 * is permitted: an accepted order may move to in-progress, an
 * in-progress order may move to done, and a completed order cannot
 * move to another status.</p>
 *
 * <p>Design approach: Enum-based state-transition rules.
 * Encapsulates the allowed workflow transitions in one place
 * without implementing the State design pattern.</p>
 */
public enum RepairStatus {
    ACCEPTED("Accepted"), IN_PROGRESS("In progress"), DONE("Done");

    private final String label;

    RepairStatus(String label) {
        this.label = label;
    }

    public boolean canMoveTo(RepairStatus next) {
        return switch (this) {
            case ACCEPTED -> next == IN_PROGRESS;
            case IN_PROGRESS -> next == DONE;
            case DONE -> false;
        };
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}