package com.fixmygadget.dao;

import com.fixmygadget.model.RepairOrder;
import com.fixmygadget.model.RepairStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface RepairOrderDao extends GenericDao<RepairOrder, Integer> {
    List<RepairOrder> findByStatus(RepairStatus status);

    List<RepairOrder> findByTechnician(int technicianId);

    List<RepairOrder> findByCustomer(int customerId);

    List<RepairOrder> findByGadget(int gadgetId);


    List<RepairOrder> findByPeriod(LocalDateTime from, LocalDateTime to);

    List<RepairOrder> findFinishedBetween(LocalDateTime from, LocalDateTime to);
}