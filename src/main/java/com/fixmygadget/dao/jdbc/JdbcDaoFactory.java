package com.fixmygadget.dao.jdbc;

import com.fixmygadget.dao.CustomerDao;
import com.fixmygadget.dao.DaoFactory;
import com.fixmygadget.dao.GadgetDao;
import com.fixmygadget.dao.RepairOrderDao;
import com.fixmygadget.dao.TechnicianDao;

public class JdbcDaoFactory implements DaoFactory {
    private final GadgetDao gadgetDao = new JdbcGadgetDao();
    private final CustomerDao customerDao = new JdbcCustomerDao();
    private final TechnicianDao technicianDao = new JdbcTechnicianDao();
    private final RepairOrderDao repairOrderDao =
            new JdbcRepairOrderDao(gadgetDao, customerDao, technicianDao);

    @Override
    public GadgetDao getGadgetDao() { return gadgetDao; }

    @Override
    public CustomerDao getCustomerDao() { return customerDao; }

    @Override
    public TechnicianDao getTechnicianDao() { return technicianDao; }

    @Override
    public RepairOrderDao getRepairOrderDao() { return repairOrderDao; }
}