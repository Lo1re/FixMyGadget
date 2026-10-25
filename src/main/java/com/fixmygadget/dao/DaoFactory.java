package com.fixmygadget.dao;

public interface DaoFactory {
    GadgetDao getGadgetDao();

    CustomerDao getCustomerDao();

    TechnicianDao getTechnicianDao();

    RepairOrderDao getRepairOrderDao();
}