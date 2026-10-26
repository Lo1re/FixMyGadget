package com.fixmygadget.service;

import com.fixmygadget.dao.CustomerDao;
import com.fixmygadget.dao.GadgetDao;
import com.fixmygadget.dao.RepairOrderDao;
import com.fixmygadget.dao.TechnicianDao;
import com.fixmygadget.exception.InvalidStatusTransitionException;
import com.fixmygadget.exception.NotFoundException;
import com.fixmygadget.model.Customer;
import com.fixmygadget.model.Gadget;
import com.fixmygadget.model.RepairOrder;
import com.fixmygadget.model.RepairOrderBuilder;
import com.fixmygadget.model.RepairStatus;
import com.fixmygadget.model.Technician;
import com.fixmygadget.sort.QuickSort;
import com.fixmygadget.sort.RepairOrderSortField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
/**
 * Manages repair orders and their business lifecycle.
 *
 * <p>Coordinates repair-order operations, handles status transitions,
 * and applies business rules related to repairs, customers, gadgets,
 * and technicians.</p>
 *
 * <p>Acts as the business logic layer between the application interface
 * and the data access layer.</p>
 */
public class RepairService {
    private static final Logger log = LoggerFactory.getLogger(RepairService.class);

    private final RepairOrderDao orderDao;
    private final GadgetDao gadgetDao;
    private final CustomerDao customerDao;
    private final TechnicianDao technicianDao;

    public RepairService(RepairOrderDao orderDao, GadgetDao gadgetDao,
                         CustomerDao customerDao, TechnicianDao technicianDao) {
        this.orderDao = orderDao;
        this.gadgetDao = gadgetDao;
        this.customerDao = customerDao;
        this.technicianDao = technicianDao;
    }

    public RepairOrder createOrder(int gadgetId, int customerId, String problem) {
        if (problem == null || problem.isBlank()) {
            throw new IllegalArgumentException("Problem description is required");
        }
        Gadget gadget = gadgetDao.findById(gadgetId);
        if (gadget == null) {
            throw new NotFoundException("Gadget not found: id=" + gadgetId);
        }
        Customer customer = customerDao.findById(customerId);
        if (customer == null) {
            throw new NotFoundException("Customer not found: id=" + customerId);
        }

        RepairOrder order = new RepairOrderBuilder()
                .gadget(gadget)
                .customer(customer)
                .problem(problem)
                .build();
        orderDao.save(order);
        log.info("Repair order created: id={}, gadget={}, customer={}",
                order.getId(), gadget, customer);
        return order;
    }

    public void assignTechnician(int orderId, int technicianId) {
        RepairOrder order = getById(orderId);
        Technician technician = technicianDao.findById(technicianId);
        if (technician == null) {
            throw new NotFoundException("Technician not found: id=" + technicianId);
        }
        order.setTechnician(technician);
        orderDao.update(order);
        log.info("Technician {} assigned to order id={}", technician, orderId);
    }

    public void setCost(int orderId, BigDecimal cost) {
        if (cost == null || cost.signum() < 0) {
            throw new IllegalArgumentException("Cost must be 0 or more");
        }
        RepairOrder order = getById(orderId);
        order.setCost(cost);
        orderDao.update(order);
        log.info("Cost {} set for order id={}", cost, orderId);
    }

    public void assignAndPrice(int orderId, Integer technicianId, BigDecimal cost) {
        if (technicianId == null && cost == null) {
            throw new IllegalArgumentException("Choose a technician or enter a cost");
        }
        if (cost != null && cost.signum() < 0) {
            throw new IllegalArgumentException("Cost must be 0 or more");
        }

        RepairOrder order = getById(orderId);
        if (technicianId != null) {
            Technician technician = technicianDao.findById(technicianId);
            if (technician == null) {
                throw new NotFoundException("Technician not found: id=" + technicianId);
            }
            order.setTechnician(technician);
        }
        if (cost != null) {
            order.setCost(cost);
        }
        orderDao.update(order);
        log.info("Order id={}: technician={}, cost={}", orderId, order.getTechnician(), order.getCost());
    }

    public void changeStatus(int orderId, RepairStatus newStatus) {
        RepairOrder order = getById(orderId);

        if (!order.getStatus().canMoveTo(newStatus)) {
            throw new InvalidStatusTransitionException(order.getStatus(), newStatus);
        }
        if (newStatus == RepairStatus.DONE && order.getTechnician() == null) {
            throw new IllegalArgumentException("Technician is required to finish the order");
        }
        if (newStatus == RepairStatus.DONE && order.getCost() == null) {
            throw new IllegalArgumentException("Cost is required to finish the order");
        }

        order.setStatus(newStatus);
        if (newStatus == RepairStatus.DONE) {
            order.setFinishedAt(LocalDateTime.now());
        }
        orderDao.update(order);
        log.info("Order id={} moved to {}", orderId, newStatus);
    }

    public void deleteOrder(int orderId) {
        getById(orderId);
        orderDao.delete(orderId);
        log.info("Order deleted: id={}", orderId);
    }


    public RepairOrder getById(int id) {
        RepairOrder order = orderDao.findById(id);
        if (order == null) {
            throw new NotFoundException("Repair order not found: id=" + id);
        }
        return order;
    }

    public List<RepairOrder> getAll() {
        return orderDao.findAll();
    }

    public List<RepairOrder> findByStatus(RepairStatus status) {
        return orderDao.findByStatus(status);
    }

    public List<RepairOrder> findByTechnician(int technicianId) {
        return orderDao.findByTechnician(technicianId);
    }

    public List<RepairOrder> findByCustomer(int customerId) {
        return orderDao.findByCustomer(customerId);
    }

    public List<RepairOrder> findByGadget(int gadgetId) {
        return orderDao.findByGadget(gadgetId);
    }

    public List<RepairOrder> findByPeriod(LocalDate from, LocalDate to) {
        return orderDao.findByPeriod(from.atStartOfDay(), to.atTime(23, 59, 59));
    }


    public List<RepairOrder> getCustomerHistory(int customerId) {
        return orderDao.findByCustomer(customerId);
    }

    public List<RepairOrder> getGadgetHistory(int gadgetId) {
        return orderDao.findByGadget(gadgetId);
    }


    public List<RepairOrder> sort(List<RepairOrder> orders, RepairOrderSortField field, boolean ascending) {
        QuickSort.sort(orders, field.getComparator(ascending));
        return orders;
    }
}