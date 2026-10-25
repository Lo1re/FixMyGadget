package com.fixmygadget;

import com.fixmygadget.dao.DaoFactory;
import com.fixmygadget.dao.jdbc.JdbcDaoFactory;
import com.fixmygadget.db.ConnectionProvider;
import com.fixmygadget.db.DatabaseInitializer;
import com.fixmygadget.facade.FixMyGadgetFacade;
import com.fixmygadget.service.CustomerService;
import com.fixmygadget.service.GadgetService;
import com.fixmygadget.service.ReportService;
import com.fixmygadget.service.RepairService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {

        ConnectionProvider.getInstance();


        new DatabaseInitializer().init();

        DaoFactory daoFactory = new JdbcDaoFactory();

        GadgetService gadgetService = new GadgetService(daoFactory.getGadgetDao());
        CustomerService customerService = new CustomerService(
                daoFactory.getCustomerDao(), daoFactory.getTechnicianDao());
        RepairService repairService = new RepairService(
                daoFactory.getRepairOrderDao(), daoFactory.getGadgetDao(),
                daoFactory.getCustomerDao(), daoFactory.getTechnicianDao());
        ReportService reportService = new ReportService(daoFactory.getRepairOrderDao());

        FixMyGadgetFacade facade = new FixMyGadgetFacade(
                gadgetService, customerService, repairService, reportService);

        log.info("FixMyGadget started");


    }
}