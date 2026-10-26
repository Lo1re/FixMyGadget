package com.fixmygadget.dao;

import com.fixmygadget.model.RepairOrder;
import com.fixmygadget.model.RepairStatus;

import java.time.LocalDateTime;
import java.util.List;
/**
 * Defines data access operations for repair orders.
 *
 * <p>Extends {@link GenericDao} with queries for retrieving repair
 * orders by status, technician, customer, gadget, creation period,
 * and completion period.</p>
 *
 * <p>Design pattern: Data Access Object (DAO).</p>
 *
 * <p>Centralizes repair-order retrieval requirements, supporting
 * workflows such as tracking repair progress, finding orders
 * associated with particular entities, and retrieving completed
 * repairs for reporting purposes.</p>
 */
public interface RepairOrderDao extends GenericDao<RepairOrder, Integer> {
    List<RepairOrder> findByStatus(RepairStatus status);

    List<RepairOrder> findByTechnician(int technicianId);

    List<RepairOrder> findByCustomer(int customerId);

    List<RepairOrder> findByGadget(int gadgetId);


    List<RepairOrder> findByPeriod(LocalDateTime from, LocalDateTime to);

    List<RepairOrder> findFinishedBetween(LocalDateTime from, LocalDateTime to);
}