package com.fixmygadget.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RepairOrder {
    private Integer id;
    private Gadget gadget;
    private Customer customer;
    private Technician technician;     //can be null
    private String problem;
    private RepairStatus status = RepairStatus.ACCEPTED;
    private BigDecimal cost;           // null before apply
    private LocalDateTime createdAt;
    private LocalDateTime finishedAt;  // null before finish

    //empty for dao and builder
    public RepairOrder() {
    }


    public RepairOrder(Gadget gadget, Customer customer, String problem) {
        this.gadget = gadget;
        this.customer = customer;
        this.problem = problem;
        this.createdAt = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Gadget getGadget() {
        return gadget;
    }

    public void setGadget(Gadget gadget) {
        this.gadget = gadget;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Technician getTechnician() {
        return technician;
    }

    public void setTechnician(Technician technician) {
        this.technician = technician;
    }

    public String getProblem() {
        return problem;
    }

    public void setProblem(String problem) {
        this.problem = problem;
    }

    public RepairStatus getStatus() {
        return status;
    }

    public void setStatus(RepairStatus status) {
        this.status = status;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    @Override
    public String toString() {
        return "Order #" + id + " [" + status + "] " + gadget + " - " + problem;
    }
}