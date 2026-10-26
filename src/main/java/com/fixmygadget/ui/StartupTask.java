package com.fixmygadget.ui;

import com.fixmygadget.dao.DaoFactory;
import com.fixmygadget.dao.jdbc.JdbcDaoFactory;
import com.fixmygadget.db.ConnectionProvider;
import com.fixmygadget.db.DatabaseInitializer;
import com.fixmygadget.facade.FixMyGadgetFacade;
import com.fixmygadget.service.CustomerService;
import com.fixmygadget.service.GadgetService;
import com.fixmygadget.service.RepairService;
import com.fixmygadget.service.ReportService;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class StartupTask extends Task<FixMyGadgetFacade> {
    private static final Logger log = LoggerFactory.getLogger(StartupTask.class);

    private static final long STEP_MS = 25;

    private double current = 0;

    @Override
    protected FixMyGadgetFacade call() throws Exception {
        updateProgress(0, 1);
        advanceTo(0.20, 700, "Starting RBK Solutions...");

        updateMessage("Connecting to database...");
        ConnectionProvider.getInstance();
        advanceTo(0.45, 700, "Connecting to database...");

        updateMessage("Preparing database...");
        new DatabaseInitializer().init();
        advanceTo(0.65, 600, "Preparing database...");

        updateMessage("Loading services...");
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
        advanceTo(0.85, 600, "Loading services...");

        advanceTo(1.0, 500, "Starting application...");
        log.info("FixMyGadget started");
        return facade;
    }

    private void advanceTo(double target, long millis, String message) throws InterruptedException {
        updateMessage(message);
        int steps = (int) Math.max(1, millis / STEP_MS);
        double delta = (target - current) / steps;
        for (int i = 0; i < steps; i++) {
            Thread.sleep(STEP_MS);
            current += delta;
            updateProgress(current, 1);
        }
        current = target;
        updateProgress(current, 1);
    }
}