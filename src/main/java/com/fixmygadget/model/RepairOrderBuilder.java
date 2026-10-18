package com.fixmygadget.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RepairOrderBuilder implements Builder<RepairOrder> {
    private Integer id;
    private Gadget gadget;
    private Customer customer;
    private Technician technician;
    private String problem;
    private RepairStatus status = RepairStatus.ACCEPTED;
    private BigDecimal cost;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime finishedAt;

    public RepairOrderBuilder id(Integer id) {
        this.id = id;
        return this;
    }

    public RepairOrderBuilder gadget(Gadget gadget) {
        this.gadget = gadget;
        return this;
    }

    public RepairOrderBuilder customer(Customer customer) {
        this.customer = customer;
        return this;
    }

    public RepairOrderBuilder technician(Technician technician) {
        this.technician = technician;
        return this;
    }

    public RepairOrderBuilder problem(String problem) {
        this.problem = problem;
        return this;
    }

    public RepairOrderBuilder status(RepairStatus status) {
        this.status = status;
        return this;
    }

    public RepairOrderBuilder cost(BigDecimal cost) {
        this.cost = cost;
        return this;
    }

    public RepairOrderBuilder createdAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public RepairOrderBuilder finishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
        return this;
    }

    @Override
    public RepairOrder build() {
        if (gadget == null || customer == null || problem == null) {
            throw new IllegalStateException("gadget, customer and problem are required");
        }
        RepairOrder order = new RepairOrder(gadget, customer, problem);
        order.setId(id);
        order.setTechnician(technician);
        order.setStatus(status);
        order.setCost(cost);
        order.setCreatedAt(createdAt);
        order.setFinishedAt(finishedAt);
        return order;
    }
}