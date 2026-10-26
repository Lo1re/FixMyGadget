package com.fixmygadget.exception;

import com.fixmygadget.model.RepairStatus;
/**
 * Represents an invalid attempt to change a repair order's status.
 *
 * <p>Thrown when a requested status transition violates the repair
 * workflow rules defined by {@link com.fixmygadget.model.RepairStatus}.</p>
 *
 * <p>Design approach: Custom unchecked exception.
 * Distinguishes invalid business operations from database failures
 * and missing entities, allowing the service layer to report
 * workflow violations explicitly.</p>
 */
public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(RepairStatus from, RepairStatus to) {
        super("Cannot change status from " + from + " to " + to);
    }
}