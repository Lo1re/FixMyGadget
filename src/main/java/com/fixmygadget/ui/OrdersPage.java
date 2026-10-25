package com.fixmygadget.ui;

import com.fixmygadget.facade.FixMyGadgetFacade;
import com.fixmygadget.model.Customer;
import com.fixmygadget.model.Gadget;
import com.fixmygadget.model.RepairOrder;
import com.fixmygadget.model.RepairStatus;
import com.fixmygadget.model.Technician;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class OrdersPage extends Page {
    private final OrdersTable ordersTable = new OrdersTable(facade);
    private final Label countLabel = new Label();

    private final ComboBox<RepairStatus> statusBox = Ui.combo("Any status");
    private final ComboBox<Technician> technicianBox = Ui.combo("Any technician");
    private final ComboBox<Customer> customerBox = Ui.combo("Any customer");
    private final ComboBox<Gadget> gadgetBox = Ui.combo("Any gadget");
    private final DatePicker fromPicker = new DatePicker();
    private final DatePicker toPicker = new DatePicker();

    public OrdersPage(FixMyGadgetFacade facade) {
        super(facade, "Repair orders", "Accept, assign, price and finish repairs");

        statusBox.getItems().setAll(RepairStatus.values());
        fromPicker.setPromptText("From date");
        toPicker.setPromptText("To date");
        fromPicker.setPrefWidth(150);
        toPicker.setPrefWidth(150);

        VBox.setVgrow(ordersTable, Priority.ALWAYS);
        countLabel.getStyleClass().add("count-label");
        getChildren().addAll(buildFilters(), buildActions(), ordersTable, countLabel);
    }

    private VBox buildFilters() {
        Label title = new Label("Filters");
        title.getStyleClass().add("section-title");

        HBox row1 = new HBox(14,
                Ui.labeled("Status", statusBox),
                Ui.labeled("Technician", technicianBox),
                Ui.labeled("Customer", customerBox),
                Ui.labeled("Gadget", gadgetBox));

        HBox period = new HBox(8, fromPicker, new Label("—"), toPicker);
        period.setAlignment(Pos.CENTER_LEFT);

        Button applyButton = Ui.button("Apply filters", "primary-button");
        applyButton.setOnAction(e -> Ui.run(this::applyFilters));
        Button resetButton = Ui.button("Reset", "secondary-button");
        resetButton.setOnAction(e -> Ui.run(this::refresh));

        HBox row2 = new HBox(14, Ui.labeled("Created between", period), Ui.spacer(), resetButton, applyButton);
        row2.setAlignment(Pos.BOTTOM_LEFT);

        VBox card = new VBox(12, title, row1, row2);
        card.getStyleClass().add("card");
        return card;
    }

    private HBox buildActions() {
        Button newButton = Ui.button("+ New order", "primary-button");
        newButton.setOnAction(e -> Ui.run(this::createOrder));
        Button assignButton = Ui.button("Assign & price", "secondary-button");
        assignButton.setOnAction(e -> Ui.run(this::assignAndPrice));

        Button startButton = Ui.button("▶ Start repair", "secondary-button");
        startButton.setOnAction(e -> Ui.run(() -> changeStatus(RepairStatus.IN_PROGRESS)));
        Button finishButton = Ui.button("✔ Finish", "secondary-button");
        finishButton.setOnAction(e -> Ui.run(() -> changeStatus(RepairStatus.DONE)));

        Button deleteButton = Ui.button("Delete", "danger-button");
        deleteButton.setOnAction(e -> Ui.run(this::deleteOrder));

        HBox actions = new HBox(12,
                Ui.buttonGroup(newButton, assignButton),
                Ui.buttonGroup(startButton, finishButton),
                Ui.spacer(),
                Ui.buttonGroup(deleteButton));
        actions.setAlignment(Pos.CENTER_LEFT);
        return actions;
    }

    private void show(List<RepairOrder> orders) {
        ordersTable.setOrders(orders);
        countLabel.setText(orders.size() + " orders");
    }

    @Override
    public void refresh() {
        statusBox.setValue(null);
        technicianBox.setValue(null);
        customerBox.setValue(null);
        gadgetBox.setValue(null);
        fromPicker.setValue(null);
        toPicker.setValue(null);

        technicianBox.getItems().setAll(facade.getAllTechnicians());
        customerBox.getItems().setAll(facade.getAllCustomers());
        gadgetBox.getItems().setAll(facade.getAllGadgets());

        show(facade.getAllOrders());
    }

    private void applyFilters() {
        List<RepairOrder> result = facade.getAllOrders();

        if (statusBox.getValue() != null) {
            result = Ui.intersect(result, facade.findOrdersByStatus(statusBox.getValue()), RepairOrder::getId);
        }
        if (technicianBox.getValue() != null) {
            result = Ui.intersect(result,
                    facade.findOrdersByTechnician(technicianBox.getValue().getId()), RepairOrder::getId);
        }
        if (customerBox.getValue() != null) {
            result = Ui.intersect(result,
                    facade.findOrdersByCustomer(customerBox.getValue().getId()), RepairOrder::getId);
        }
        if (gadgetBox.getValue() != null) {
            result = Ui.intersect(result,
                    facade.findOrdersByGadget(gadgetBox.getValue().getId()), RepairOrder::getId);
        }
        if (fromPicker.getValue() != null || toPicker.getValue() != null) {
            LocalDate from = fromPicker.getValue();
            LocalDate to = toPicker.getValue();
            if (from == null) {
                from = LocalDate.of(2000, 1, 1);
            }
            if (to == null) {
                to = LocalDate.now();
            }
            if (from.isAfter(to)) {
                throw new IllegalArgumentException("'From' date must not be after 'to' date");
            }
            result = Ui.intersect(result, facade.findOrdersByPeriod(from, to), RepairOrder::getId);
        }

        show(result);
    }

    private RepairOrder selected() {
        RepairOrder order = ordersTable.getSelectedOrder();
        if (order == null) {
            throw new IllegalArgumentException("Select an order in the table first");
        }
        return order;
    }

    private void createOrder() {
        List<Gadget> gadgets = facade.getAllGadgets();
        List<Customer> customers = facade.getAllCustomers();
        if (gadgets.isEmpty() || customers.isEmpty()) {
            throw new IllegalArgumentException("Add at least one gadget and one customer first");
        }

        ComboBox<Gadget> gadgetChoice = Ui.combo("Select gadget");
        gadgetChoice.getItems().setAll(gadgets);
        gadgetChoice.setPrefWidth(340);
        ComboBox<Customer> customerChoice = Ui.combo("Select customer");
        customerChoice.getItems().setAll(customers);
        customerChoice.setPrefWidth(340);
        TextArea problemArea = new TextArea();
        problemArea.setPromptText("Describe the problem");
        problemArea.setPrefRowCount(4);
        problemArea.setPrefWidth(340);
        problemArea.setWrapText(true);

        GridPane grid = Ui.form();
        Ui.addRow(grid, 0, "Gadget", gadgetChoice);
        Ui.addRow(grid, 1, "Customer", customerChoice);
        Ui.addRow(grid, 2, "Problem", problemArea);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("New repair order");
        dialog.setHeaderText("Accept a gadget for repair");
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Ui.style(dialog);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (gadgetChoice.getValue() == null || customerChoice.getValue() == null) {
                throw new IllegalArgumentException("Select a gadget and a customer");
            }
            facade.createOrder(gadgetChoice.getValue().getId(),
                    customerChoice.getValue().getId(), problemArea.getText());
            applyFilters();
        }
    }

    private void assignAndPrice() {
        RepairOrder order = selected();
        List<Technician> technicians = facade.getAllTechnicians();
        if (technicians.isEmpty()) {
            throw new IllegalArgumentException("Add a technician first");
        }

        ComboBox<Technician> technicianChoice = Ui.combo("Select technician");
        technicianChoice.getItems().setAll(technicians);
        technicianChoice.setPrefWidth(260);
        for (Technician technician : technicians) {
            if (order.getTechnician() != null && technician.getId().equals(order.getTechnician().getId())) {
                technicianChoice.setValue(technician);
            }
        }

        TextField costField = new TextField();
        costField.setPromptText("0.00");
        InputFilters.moneyOnly(costField);
        if (order.getCost() != null) {
            costField.setText(order.getCost().toPlainString());
        }

        GridPane grid = Ui.form();
        Ui.addRow(grid, 0, "Technician", technicianChoice);
        Ui.addRow(grid, 1, "Cost (₴)", costField);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Assign & price");
        dialog.setHeaderText("Order #" + order.getId() + " · " + order.getGadget().getName());
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Ui.style(dialog);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Integer technicianId = null;
            if (technicianChoice.getValue() != null) {
                technicianId = technicianChoice.getValue().getId();
            }
            BigDecimal cost = null;
            if (!costField.getText().isBlank()) {
                cost = Ui.parseMoney(costField.getText(), "Cost");
            }
            facade.assignAndPrice(order.getId(), technicianId, cost);
            applyFilters();
        }
    }

    private void changeStatus(RepairStatus newStatus) {
        RepairOrder order = selected();
        facade.changeStatus(order.getId(), newStatus);
        applyFilters();
    }

    private void deleteOrder() {
        RepairOrder order = selected();
        if (Ui.confirm("Delete order #" + order.getId() + "?")) {
            facade.deleteOrder(order.getId());
            applyFilters();
        }
    }
}