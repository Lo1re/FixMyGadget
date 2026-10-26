package com.fixmygadget.service;

import com.fixmygadget.dao.CustomerDao;
import com.fixmygadget.dao.TechnicianDao;
import com.fixmygadget.exception.NotFoundException;
import com.fixmygadget.model.Customer;
import com.fixmygadget.model.Technician;
import com.fixmygadget.sort.CustomerSortField;
import com.fixmygadget.sort.QuickSort;
import com.fixmygadget.sort.TechnicianSortField;
import com.fixmygadget.util.InputRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CustomerService {
    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerDao customerDao;
    private final TechnicianDao technicianDao;

    public CustomerService(CustomerDao customerDao, TechnicianDao technicianDao) {
        this.customerDao = customerDao;
        this.technicianDao = technicianDao;
    }



    public void addCustomer(Customer customer) {
        validateCustomer(customer);
        if (customerDao.findByPhone(customer.getPhone()) != null) {
            throw new IllegalArgumentException("Customer with this phone already exists");
        }
        customerDao.save(customer);
        log.info("Customer added: {}", customer);
    }

    public void updateCustomer(Customer customer) {
        if (customer.getId() == null) {
            throw new IllegalArgumentException("Customer has no id");
        }
        validateCustomer(customer);
        getCustomerById(customer.getId());

        Customer withSamePhone = customerDao.findByPhone(customer.getPhone());
        if (withSamePhone != null && !withSamePhone.getId().equals(customer.getId())) {
            throw new IllegalArgumentException("Customer with this phone already exists");
        }
        customerDao.update(customer);
        log.info("Customer updated: {}", customer);
    }

    public void deleteCustomer(int id) {
        getCustomerById(id);
        customerDao.delete(id);
        log.info("Customer deleted: id={}", id);
    }

    public Customer getCustomerById(int id) {
        Customer customer = customerDao.findById(id);
        if (customer == null) {
            throw new NotFoundException("Customer not found: id=" + id);
        }
        return customer;
    }

    public List<Customer> getAllCustomers() {
        return customerDao.findAll();
    }

    public List<Customer> sortCustomers(List<Customer> customers, CustomerSortField field, boolean ascending) {
        QuickSort.sort(customers, field.getComparator(ascending));
        return customers;
    }



    public void addTechnician(Technician technician) {
        validateTechnician(technician);
        technicianDao.save(technician);
        log.info("Technician added: {}", technician);
    }

    public void updateTechnician(Technician technician) {
        if (technician.getId() == null) {
            throw new IllegalArgumentException("Technician has no id");
        }
        validateTechnician(technician);
        getTechnicianById(technician.getId());
        technicianDao.update(technician);
        log.info("Technician updated: {}", technician);
    }

    public void deleteTechnician(int id) {
        getTechnicianById(id);
        technicianDao.delete(id);
        log.info("Technician deleted: id={}", id);
    }

    public Technician getTechnicianById(int id) {
        Technician technician = technicianDao.findById(id);
        if (technician == null) {
            throw new NotFoundException("Technician not found: id=" + id);
        }
        return technician;
    }

    public List<Technician> getAllTechnicians() {
        return technicianDao.findAll();
    }

    public List<Technician> sortTechnicians(List<Technician> technicians, TechnicianSortField field,
                                            boolean ascending) {
        QuickSort.sort(technicians, field.getComparator(ascending));
        return technicians;
    }


    private void validateCustomer(Customer customer) {
        validateName(customer.getLastName(), "Customer last name");
        validateName(customer.getFirstName(), "Customer first name");
        if (customer.getPhone() == null || customer.getPhone().isBlank()) {
            throw new IllegalArgumentException("Customer phone is required");
        }
        if (!InputRules.isValidPhone(customer.getPhone())) {
            throw new IllegalArgumentException("Phone must look like +380XXXXXXXXX (+380 and 9 digits)");
        }
    }

    private void validateTechnician(Technician technician) {
        validateName(technician.getLastName(), "Technician last name");
        validateName(technician.getFirstName(), "Technician first name");
    }

    private void validateName(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        if (!InputRules.isValidName(value)) {
            throw new IllegalArgumentException(fieldName + " must contain only letters");
        }
    }
}