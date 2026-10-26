package com.fixmygadget.dao.jdbc;

import com.fixmygadget.dao.CustomerDao;
import com.fixmygadget.dao.DaoFactory;
import com.fixmygadget.dao.GadgetDao;
import com.fixmygadget.dao.RepairOrderDao;
import com.fixmygadget.dao.TechnicianDao;
/**
 * Provides JDBC-based implementations of the application's DAO
 * interfaces.
 *
 * <p>Creates and retains DAO instances for gadgets, customers,
 * technicians, and repair orders. Supplies these instances through
 * the corresponding methods defined by {@link com.fixmygadget.dao.DaoFactory}.
 * The repair-order DAO receives the other DAO instances through
 * constructor injection to resolve related entities.</p>
 *
 * <p>Design patterns: Factory and Singleton-like instance reuse
 * within the factory instance. Implements the
 * {@link com.fixmygadget.dao.DaoFactory} abstraction.</p>
 *
 * <p>Centralizes the selection of JDBC persistence implementations,
 * reduces coupling between application code and concrete DAO classes,
 * and reuses DAO objects instead of constructing new ones on every
 * factory method call.</p>
 */
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