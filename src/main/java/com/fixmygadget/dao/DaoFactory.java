package com.fixmygadget.dao;
/**
 * Defines a factory contract for obtaining data access objects.
 *
 * <p>Provides access to the DAO interfaces for gadgets, customers,
 * technicians, and repair orders without exposing the concrete
 * implementation classes to the caller.</p>
 *
 * <p>Design pattern: Abstract Factory-style abstraction.</p>
 *
 * <p>Centralizes the creation and provision of related DAO
 * components behind a common contract, allowing application code
 * to depend on interfaces rather than database-specific classes.</p>
 */
public interface DaoFactory {
    GadgetDao getGadgetDao();

    CustomerDao getCustomerDao();

    TechnicianDao getTechnicianDao();

    RepairOrderDao getRepairOrderDao();
}