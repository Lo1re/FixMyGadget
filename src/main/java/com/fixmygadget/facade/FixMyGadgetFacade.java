package com.fixmygadget.facade;

import com.fixmygadget.model.Customer;
import com.fixmygadget.model.Feature;
import com.fixmygadget.model.Gadget;
import com.fixmygadget.model.GadgetType;
import com.fixmygadget.model.RepairOrder;
import com.fixmygadget.model.RepairStatus;
import com.fixmygadget.model.Technician;
import com.fixmygadget.service.CustomerService;
import com.fixmygadget.service.GadgetService;
import com.fixmygadget.service.ReportService;
import com.fixmygadget.service.RepairService;
import com.fixmygadget.sort.GadgetSortField;
import com.fixmygadget.sort.RepairOrderSortField;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class FixMyGadgetFacade {
    private final GadgetService gadgetService;
    private final CustomerService customerService;
    private final RepairService repairService;
    private final ReportService reportService;

    public FixMyGadgetFacade(GadgetService gadgetService, CustomerService customerService,
                             RepairService repairService, ReportService reportService) {
        this.gadgetService = gadgetService;
        this.customerService = customerService;
        this.repairService = repairService;
        this.reportService = reportService;
    }


    public void addGadget(Gadget gadget) { gadgetService.addGadget(gadget); }

    public void updateGadget(Gadget gadget) { gadgetService.updateGadget(gadget); }

    public void deleteGadget(int id) { gadgetService.deleteGadget(id); }

    public List<Gadget> getAllGadgets() { return gadgetService.getAll(); }

    public List<Gadget> findGadgets(String name, GadgetType type) {
        return gadgetService.findByNameAndType(name, type);
    }

    public List<Gadget> findGadgetsByManufacturer(String manufacturer) {
        return gadgetService.findByManufacturer(manufacturer);
    }

    public List<Gadget> findGadgetsByType(GadgetType type) {
        return gadgetService.findByType(type);
    }

    public List<Gadget> findGadgetsByScreenSize(int x1, int y1, int x2, int y2) {
        return gadgetService.findByScreenSize(x1, y1, x2, y2);
    }

    public void addFeature(int gadgetId, Feature feature) { gadgetService.addFeature(gadgetId, feature); }

    public void removeFeature(int gadgetId, Feature feature) { gadgetService.removeFeature(gadgetId, feature); }

    public List<Gadget> sortGadgets(List<Gadget> gadgets, GadgetSortField field, boolean ascending) {
        return gadgetService.sort(gadgets, field, ascending);
    }

    public void exportCatalog(Writer writer) throws IOException { gadgetService.exportCatalog(writer); }

    public int importCatalog(Reader reader) throws IOException { return gadgetService.importCatalog(reader); }


    public void addCustomer(Customer customer) { customerService.addCustomer(customer); }

    public void updateCustomer(Customer customer) { customerService.updateCustomer(customer); }

    public void deleteCustomer(int id) { customerService.deleteCustomer(id); }

    public List<Customer> getAllCustomers() { return customerService.getAllCustomers(); }


    public void addTechnician(Technician technician) { customerService.addTechnician(technician); }

    public void updateTechnician(Technician technician) { customerService.updateTechnician(technician); }

    public void deleteTechnician(int id) { customerService.deleteTechnician(id); }

    public List<Technician> getAllTechnicians() { return customerService.getAllTechnicians(); }



    public RepairOrder createOrder(int gadgetId, int customerId, String problem) {
        return repairService.createOrder(gadgetId, customerId, problem);
    }

    public void assignTechnician(int orderId, int technicianId) {
        repairService.assignTechnician(orderId, technicianId);
    }

    public void setCost(int orderId, BigDecimal cost) { repairService.setCost(orderId, cost); }

    public void changeStatus(int orderId, RepairStatus status) { repairService.changeStatus(orderId, status); }

    public void deleteOrder(int orderId) { repairService.deleteOrder(orderId); }

    public List<RepairOrder> getAllOrders() { return repairService.getAll(); }

    public List<RepairOrder> findOrdersByStatus(RepairStatus status) { return repairService.findByStatus(status); }

    public List<RepairOrder> findOrdersByTechnician(int technicianId) {
        return repairService.findByTechnician(technicianId);
    }

    public List<RepairOrder> findOrdersByCustomer(int customerId) {
        return repairService.findByCustomer(customerId);
    }

    public List<RepairOrder> findOrdersByGadget(int gadgetId) { return repairService.findByGadget(gadgetId); }

    public List<RepairOrder> findOrdersByPeriod(LocalDate from, LocalDate to) {
        return repairService.findByPeriod(from, to);
    }

    public List<RepairOrder> getCustomerHistory(int customerId) { return repairService.getCustomerHistory(customerId); }

    public List<RepairOrder> getGadgetHistory(int gadgetId) { return repairService.getGadgetHistory(gadgetId); }

    public List<RepairOrder> sortOrders(List<RepairOrder> orders, RepairOrderSortField field, boolean ascending) {
        return repairService.sort(orders, field, ascending);
    }


    public BigDecimal getRevenue(LocalDate from, LocalDate to) { return reportService.getRevenue(from, to); }

    public List<RepairOrder> getOrdersByTechnician(int technicianId) {
        return reportService.getOrdersByTechnician(technicianId);
    }

    public int countOrdersByTechnician(int technicianId) {
        return reportService.countOrdersByTechnician(technicianId);
    }
}