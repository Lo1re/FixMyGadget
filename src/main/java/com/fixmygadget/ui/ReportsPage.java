package com.fixmygadget.ui;

import com.fixmygadget.facade.FixMyGadgetFacade;
import com.fixmygadget.model.RepairOrder;
import com.fixmygadget.model.Technician;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;

public class ReportsPage extends Page {
    private final DatePicker fromPicker = new DatePicker();
    private final DatePicker toPicker = new DatePicker();
    private final Label revenueLabel = new Label("—");

    private final ComboBox<Technician> technicianBox = Ui.combo("Select technician");
    private final Label countLabel = new Label("—");
    private final OrdersTable ordersTable = new OrdersTable();

    public ReportsPage(FixMyGadgetFacade facade) {
        super(facade, "Reports", "Revenue and workload of technicians");

        Label revenueTitle = new Label("Revenue for a period");
        revenueTitle.getStyleClass().add("section-title");
        Label revenueHint = new Label("Sum of the cost of finished orders");
        revenueHint.getStyleClass().add("count-label");

        Button revenueButton = Ui.button("Calculate", "primary-button");
        revenueButton.setOnAction(e -> Ui.run(this::calculateRevenue));
        revenueLabel.getStyleClass().add("big-value");

        HBox revenueRow = new HBox(12, fromPicker, toPicker, revenueButton);
        revenueRow.setAlignment(Pos.CENTER_LEFT);
        VBox revenueCard = new VBox(10, revenueTitle, revenueHint, revenueRow, revenueLabel);
        revenueCard.getStyleClass().add("card");

        Label technicianTitle = new Label("Orders of a technician");
        technicianTitle.getStyleClass().add("section-title");

        Button showButton = Ui.button("Show orders", "primary-button");
        showButton.setOnAction(e -> Ui.run(this::showTechnicianOrders));
        countLabel.getStyleClass().add("big-value");
        technicianBox.setPrefWidth(260);

        HBox technicianRow = new HBox(12, technicianBox, showButton, countLabel);
        technicianRow.setAlignment(Pos.CENTER_LEFT);
        VBox technicianCard = new VBox(10, technicianTitle, technicianRow, ordersTable);
        technicianCard.getStyleClass().add("card");
        VBox.setVgrow(ordersTable, Priority.ALWAYS);
        VBox.setVgrow(technicianCard, Priority.ALWAYS);

        getChildren().addAll(revenueCard, technicianCard);
    }

    @Override
    public void refresh() {
        LocalDate today = LocalDate.now();
        fromPicker.setValue(today.withDayOfMonth(1));
        toPicker.setValue(today);
        revenueLabel.setText("—");

        technicianBox.getItems().setAll(facade.getAllTechnicians());
        technicianBox.setValue(null);
        countLabel.setText("—");
        ordersTable.getItems().clear();
    }

    private void calculateRevenue() {
        if (fromPicker.getValue() == null || toPicker.getValue() == null) {
            throw new IllegalArgumentException("Choose both dates");
        }
        revenueLabel.setText(Ui.money(facade.getRevenue(fromPicker.getValue(), toPicker.getValue())));
    }

    private void showTechnicianOrders() {
        Technician technician = technicianBox.getValue();
        if (technician == null) {
            throw new IllegalArgumentException("Choose a technician first");
        }
        List<RepairOrder> orders = facade.getOrdersByTechnician(technician.getId());
        countLabel.setText(facade.countOrdersByTechnician(technician.getId()) + " orders");
        ordersTable.setOrders(orders);
    }
}