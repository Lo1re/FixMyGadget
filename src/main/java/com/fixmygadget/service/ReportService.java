package com.fixmygadget.service;

import com.fixmygadget.dao.RepairOrderDao;
import com.fixmygadget.model.RepairOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ReportService {
    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final RepairOrderDao orderDao;

    public ReportService(RepairOrderDao orderDao) {
        this.orderDao = orderDao;
    }

    public BigDecimal getRevenue(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("Start date must not be after end date");
        }
        List<RepairOrder> finished = orderDao.findFinishedBetween(from.atStartOfDay(), to.atTime(23, 59, 59));

        BigDecimal total = BigDecimal.ZERO;
        for (RepairOrder order : finished) {
            total = total.add(order.getCost());
        }
        log.info("Revenue report {} - {}: {}", from, to, total);
        return total;
    }

    public List<RepairOrder> getOrdersByTechnician(int technicianId) {
        return orderDao.findByTechnician(technicianId);
    }

    public int countOrdersByTechnician(int technicianId) {
        return orderDao.findByTechnician(technicianId).size();
    }
}