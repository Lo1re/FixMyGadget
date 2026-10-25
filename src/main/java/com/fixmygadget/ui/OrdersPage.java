package com.fixmygadget.ui;

import com.fixmygadget.facade.FixMyGadgetFacade;
import com.fixmygadget.model.Customer;
import com.fixmygadget.model.Gadget;
import com.fixmygadget.model.RepairOrder;
import com.fixmygadget.model.RepairStatus;
import com.fixmygadget.model.Technician;
import com.fixmygadget.sort.RepairOrderSortField;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class OrdersPage extends Page {
    private final TableView<RepairOrder> table = new TableView<>();
    private final Map<RepairOrderSortField, Label> headers = new HashMap<>();
    private final Label countLabel = new Label();

    private final ComboBox<RepairStatus> statusBox = Ui.combo("Any status");
    private final ComboBox<Technician> technicianBox = Ui.combo("Any technician");
    private final ComboBox<Customer> customerBox = Ui.combo("Any customer");
    private final ComboBox<Gadget> gadgetBox = Ui.combo("Any gadget");
    private final DatePicker fromPicker = new DatePicker();
    private final DatePicker toPicker = new DatePicker();

    private List<RepairOrder> current = new ArrayList<>();
    private RepairOrderSortField sortField = null;
    private boolean ascending = true;

    public OrdersPage(FixMyGadgetFacade facade) {
        super(facade, "Repair orders", "Accept, assign, price and finish repairs");

        statusBox.getItems().setAll(RepairStatus.values());
        fromPicker.setPromptText("From date");
        toPicker.setPromptText("To date");
        fromPicker.setPrefWidth(150);
        toPicker.setPrefWidth(150);

        buildTable();
        getChildren().addAll(buildFilters(), buildActions(), table, countLabel);
        countLabel.getStyleClass().add("count-label");
    }



    private VBox buildFilters() {
        Button applyButton = Ui.button("Apply filters", "primary-button");
        applyButton.setOnAction(e -> Ui.run(this::applyFilters));
        Button resetButton = Ui.button("Reset", "secondary-button");
        resetButton.setOnAction(e -> Ui.run(this::refresh));

        FlowPane flow = new FlowPane(10, 10, statusBox, technicianBox, customerBox, gadgetBox,
                fromPicker, toPicker, applyButton, resetButton);

        Label title = new Label("Filters");
        title.getStyleClass().add("section-title");

        VBox card = new VBox(12, title, flow);
        card.getStyleClass().add("card");
        return card;
    }

    private HBox buildActions() {
        Button newButton = Ui.button("+ New order", "primary-button");
        newButton.setOnAction(e -> Ui.run(this::createOrder));
        Button assignButton = Ui.button("Assign technician", "secondary-button");
        assignButton.setOnAction(e -> Ui.run(this::assignTechnician));
        Button costButton = Ui.button("Set cost", "secondary-button");
        costButton.setOnAction(e -> Ui.run(this::setCost));
        Button startButton = Ui.button("▶ Start repair", "secondary-button");
        startButton.setOnAction(e -> Ui.run(() -> changeStatus(RepairStatus.IN_PROGRESS)));
        Button finishButton = Ui.button("✔ Finish", "secondary-button");
        finishButton.setOnAction(e -> Ui.run(() -> changeStatus(RepairStatus.DONE)));
        Button deleteButton = Ui.button("Delete", "danger-button");
        deleteButton.setOnAction(e -> Ui.run(this::deleteOrder));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return new HBox(10, newButton, assignButton, costButton, startButton, finishButton, spacer, deleteButton);
    }

    private void buildTable() {
        TableColumn<RepairOrder, String> idColumn =
                sortable(RepairOrderSortField.ID, 70, o -> String.valueOf(o.getId()));
        TableColumn<RepairOrder, String> gadgetColumn =
                sortable(RepairOrderSortField.GADGET, 180, o -> o.getGadget().getName());
        TableColumn<RepairOrder, String> customerColumn =
                sortable(RepairOrderSortField.CUSTOMER, 170, o -> Ui.customerName(o.getCustomer()));
        TableColumn<RepairOrder, String> technicianColumn =
                sortable(RepairOrderSortField.TECHNICIAN, 160, o -> Ui.technicianName(o.getTechnician()));

        TableColumn<RepairOrder, RepairStatus> statusColumn = Ui.statusColumn(null, 130);
        addHeader(statusColumn, RepairOrderSortField.STATUS);

        TableColumn<RepairOrder, String> costColumn =
                sortable(RepairOrderSortField.COST, 110, o -> Ui.money(o.getCost()));
        TableColumn<RepairOrder, String> createdColumn =
                sortable(RepairOrderSortField.CREATED_AT, 140, o -> Ui.date(o.getCreatedAt()));
        TableColumn<RepairOrder, String> finishedColumn =
                sortable(RepairOrderSortField.FINISHED_AT, 140, o -> Ui.date(o.getFinishedAt()));
        TableColumn<RepairOrder, String> problemColumn =
                Ui.textColumn("Problem", 240, o -> o.getProblem());

        table.getColumns().addAll(idColumn, gadgetColumn, customerColumn, technicianColumn,
                statusColumn, costColumn, createdColumn, finishedColumn, problemColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No orders found"));
        VBox.setVgrow(table, Priority.ALWAYS);
    }

    private TableColumn<RepairOrder, String> sortable(RepairOrderSortField field, double width,
                                                      Function<RepairOrder, String> getter) {
        TableColumn<RepairOrder, String> column = Ui.textColumn(null, width, getter);
        addHeader(column, field);
        return column;
    }

    private void addHeader(TableColumn<RepairOrder, ?> column, RepairOrderSortField field) {
        Label header = new Label(field.getLabel());
        header.getStyleClass().add("sort-header");
        header.setOnMouseClicked(e -> Ui.run(() -> sortBy(field)));
        column.setGraphic(header);
        headers.put(field, header);
    }



    private void sortBy(RepairOrderSortField field) {
        if (sortField == field) {
            ascending = !ascending;
        } else {
            sortField = field;
            ascending = true;
        }
        for (RepairOrderSortField f : RepairOrderSortField.values()) {
            Label header = headers.get(f);
            String arrow = "";
            if (f == sortField) {
                arrow = ascending ? "  ▲" : "  ▼";
            }
            header.setText(f.getLabel() + arrow);
        }
        showCurrent();
    }

    private void showCurrent() {
        if (sortField != null) {
            facade.sortOrders(current, sortField, ascending);   // Quick sort в Java
        }
        table.getItems().setAll(current);
        countLabel.setText(current.size() + " orders");
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

        current = facade.getAllOrders();
        showCurrent();
    }

    private void applyFilters() {
        List<RepairOrder> result = facade.getAllOrders();

        if (statusBox.getValue() != null) {
            result = intersect(result, facade.findOrdersByStatus(statusBox.getValue()));
        }
        if (technicianBox.getValue() != null) {
            result = intersect(result, facade.findOrdersByTechnician(technicianBox.getValue().getId()));
        }
        if (customerBox.getValue() != null) {
            result = intersect(result, facade.findOrdersByCustomer(customerBox.getValue().getId()));
        }
        if (gadgetBox.getValue() != null) {
            result = intersect(result, facade.findOrdersByGadget(gadgetBox.getValue().getId()));
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
            result = intersect(result, facade.findOrdersByPeriod(from, to));
        }

        current = result;
        showCurrent();
    }

    private List<RepairOrder> intersect(List<RepairOrder> first, List<RepairOrder> second) {
        List<RepairOrder> result = new ArrayList<>();
        for (RepairOrder a : first) {
            for (RepairOrder b : second) {
                if (a.getId().equals(b.getId())) {
                    result.add(a);
                    break;
                }
            }
        }
        return result;
    }


    private RepairOrder selected() {
        RepairOrder order = table.getSelectionModel().getSelectedItem();
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

    private void assignTechnician() {
        RepairOrder order = selected();
        List<Technician> technicians = facade.getAllTechnicians();
        if (technicians.isEmpty()) {
            throw new IllegalArgumentException("Add a technician first");
        }
        ChoiceDialog<Technician> dialog = new ChoiceDialog<>(technicians.get(0), technicians);
        dialog.setTitle("Assign technician");
        dialog.setHeaderText("Order #" + order.getId() + " · " + order.getGadget().getName());
        dialog.setContentText("Technician:");
        Ui.style(dialog);

        Optional<Technician> result = dialog.showAndWait();
        if (result.isPresent()) {
            facade.assignTechnician(order.getId(), result.get().getId());
            applyFilters();
        }
    }

    private void setCost() {
        RepairOrder order = selected();
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Repair cost");
        dialog.setHeaderText("Order #" + order.getId() + " · " + order.getGadget().getName());
        dialog.setContentText("Cost (₴):");
        Ui.style(dialog);

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            facade.setCost(order.getId(), Ui.parseMoney(result.get(), "Cost"));
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