package com.fixmygadget.dao;

import com.fixmygadget.model.Customer;
/**
 * Defines data access operations specific to customer entities.
 *
 * <p>Extends {@link GenericDao} with customer-specific retrieval
 * by phone number, in addition to the common CRUD operations.</p>
 *
 * <p>Design pattern: Data Access Object (DAO).</p>
 *
 * <p>Provides a dedicated contract for customer persistence and
 * allows application services to retrieve customers using their
 * phone numbers without depending on SQL or JDBC implementation
 * details.</p>
 */
public interface CustomerDao extends GenericDao<Customer, Integer> {
    Customer findByPhone(String phone);
}