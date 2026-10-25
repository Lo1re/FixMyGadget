package com.fixmygadget.ui;

import com.fixmygadget.facade.FixMyGadgetFacade;
import com.fixmygadget.model.RepairOrder;
import com.fixmygadget.model.RepairStatus;
import com.fixmygadget.sort.RepairOrderSortField;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class OrdersTable extends TableView<RepairOrder> {
    private final FixMyGadgetFacade facade;
    private final Map<RepairOrderSortField, Label> headers = new HashMap<>();

    private List<RepairOrder> orders = new ArrayList<>();
    private RepairOrderSortField sortField = null;
    private boolean ascending = true;

    public OrdersTable(FixMyGadgetFacade facade) {
        this.facade = facade;

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

        getColumns().addAll(idColumn, gadgetColumn, customerColumn, technicianColumn,
                statusColumn, costColumn, createdColumn, finishedColumn, problemColumn);
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        setPlaceholder(new Label("No orders found"));
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
        showOrders();
    }

    private void showOrders() {
        if (sortField != null) {
            facade.sortOrders(orders, sortField, ascending);   // Quick sort в Java
        }
        getItems().setAll(orders);
    }

    public void setOrders(List<RepairOrder> newOrders) {
        orders = new ArrayList<>(newOrders);
        showOrders();
    }

    public RepairOrder getSelectedOrder() {
        return getSelectionModel().getSelectedItem();
    }
}