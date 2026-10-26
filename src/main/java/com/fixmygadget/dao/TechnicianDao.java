package com.fixmygadget.dao;

import com.fixmygadget.model.Technician;
/**
 * Defines the data access contract for technician entities.
 *
 * <p>Inherits common CRUD operations from {@link GenericDao}
 * without introducing additional technician-specific queries.</p>
 *
 * <p>Design pattern: Data Access Object (DAO).</p>
 *
 * <p>Provides a clear persistence boundary for technician records
 * and allows the underlying data access implementation to change
 * without affecting its consumers.</p>
 */
public interface TechnicianDao extends GenericDao<Technician, Integer> {
}