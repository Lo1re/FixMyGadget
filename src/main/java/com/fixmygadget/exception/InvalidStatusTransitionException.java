package com.fixmygadget.exception;

import com.fixmygadget.model.RepairStatus;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(RepairStatus from, RepairStatus to) {
        super("Cannot change status from " + from + " to " + to);
    }
}