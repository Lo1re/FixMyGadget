package com.fixmygadget.exception;
/**
 * Represents a technician employed by the FixMyGadget service center.
 *
 * <p>Stores the technician's identifier, first name, and last name.
 * Provides constructors, accessor methods, and a human-readable
 * string representation.</p>
 *
 * <p>Role: Domain model. Technician instances can be associated
 * with repair orders to identify the person responsible for
 * carrying out a repair.</p>
 */
public class DaoException extends RuntimeException {
    public DaoException(String message) { super(message); }
    public DaoException(String message, Throwable cause) { super(message, cause); }
}