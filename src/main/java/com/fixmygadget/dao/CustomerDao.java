package com.fixmygadget.dao;

import com.fixmygadget.model.Customer;

public interface CustomerDao extends GenericDao<Customer, Integer> {
    Customer findByPhone(String phone);   // null, якщо не знайдено
}