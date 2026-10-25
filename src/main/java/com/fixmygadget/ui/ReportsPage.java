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
    private final OrdersTable ordersTable = new OrdersTable(facade);

    public ReportsPage(FixMyGadgetFacade facade) {
        super(facade, "Reports", "Revenue and workload of technicians");
        getChildren().addAll(buildRevenueCard(), buildTechnicianCard());
    }

    private VBox buildRevenueCard() {
        Label title = new Label("Revenue for a period");
        title.getStyleClass().add("section-title");
        Label hint = new Label("Sum of the cost of finished orders");
        hint.getStyleClass().add("count-label");

        fromPicker.setPrefWidth(150);
        toPicker.setPrefWidth(150);
        HBox period = new HBox(8, fromPicker, new Label("—"), toPicker);
        period.setAlignment(Pos.CENTER_LEFT);

        Button calculateButton = Ui.button("Calculate", "primary-button");
        calculateButton.setOnAction(e -> Ui.run(this::calculateRevenue));
        revenueLabel.getStyleClass().add("big-value");

        HBox row = new HBox(14, Ui.labeled("From — to", period), calculateButton);
        row.setAlignment(Pos.BOTTOM_LEFT);

        VBox card = new VBox(10, title, hint, row, revenueLabel);
        card.getStyleClass().add("card");
        return card;
    }

    private VBox buildTechnicianCard() {
        Label title = new Label("Orders of a technician");
        title.getStyleClass().add("section-title");

        technicianBox.setPrefWidth(260);
        Button showButton = Ui.button("Show orders", "primary-button");
        showButton.setOnAction(e -> Ui.run(this::showTechnicianOrders));
        countLabel.getStyleClass().add("big-value");

        HBox row = new HBox(14, Ui.labeled("Technician", technicianBox), showButton, countLabel);
        row.setAlignment(Pos.BOTTOM_LEFT);

        VBox.setVgrow(ordersTable, Priority.ALWAYS);
        VBox card = new VBox(10, title, row, ordersTable);
        card.getStyleClass().add("card");
        VBox.setVgrow(card, Priority.ALWAYS);
        return card;
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
        ordersTable.setOrders(List.of());
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