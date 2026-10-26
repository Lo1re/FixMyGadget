package com.fixmygadget.exception;
/**
 * Represents an error that occurs when a requested entity cannot
 * be found.
 *
 * <p>Used by application services to signal that a requested customer,
 * gadget, technician, or repair order does not exist or cannot be
 * retrieved.</p>
 *
 * <p>Design approach: Custom unchecked exception.
 * Provides a domain-level way to report missing resources without
 * exposing database-specific error details to callers.</p>
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) { super(message); }
}