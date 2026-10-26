package com.fixmygadget.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
/**
 * Represents a repair request registered with the service center.
 *
 * <p>Stores the repair order's identifier, gadget, customer, assigned
 * technician, problem description, status, repair cost, creation
 * timestamp, and completion timestamp. A technician may be unassigned,
 * and the cost and completion timestamp may remain unset until the
 * corresponding information becomes available.</p>
 *
 * <p>The initial status is {@link RepairStatus#ACCEPTED}. The
 * parameterized constructor records the creation time using
 * {@code LocalDateTime.now()}.</p>
 *
 * <p>Design patterns: Builder is supported by the separate
 * {@link RepairOrderBuilder} class. The model represents the central
 * domain object connecting customers, gadgets, technicians, and
 * the repair workflow.</p>
 *
 * <p>Provides accessors for managing repair order data. Status
 * transition validation is defined separately by {@link RepairStatus}
 * and is not automatically enforced by this model's setter.</p>
 */
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