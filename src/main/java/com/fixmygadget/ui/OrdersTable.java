package com.fixmygadget.ui;

import com.fixmygadget.model.RepairOrder;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

import java.util.List;

public class OrdersTable extends TableView<RepairOrder> {

    public OrdersTable() {
        getColumns().add(Ui.textColumn("#", 60, o -> String.valueOf(o.getId())));
        getColumns().add(Ui.textColumn("Gadget", 180, o -> o.getGadget().getName()));
        getColumns().add(Ui.textColumn("Customer", 160, o -> Ui.customerName(o.getCustomer())));
        getColumns().add(Ui.textColumn("Technician", 150, o -> Ui.technicianName(o.getTechnician())));
        getColumns().add(Ui.statusColumn("Status", 120));
        getColumns().add(Ui.textColumn("Cost", 100, o -> Ui.money(o.getCost())));
        getColumns().add(Ui.textColumn("Created", 140, o -> Ui.date(o.getCreatedAt())));
        getColumns().add(Ui.textColumn("Finished", 140, o -> Ui.date(o.getFinishedAt())));
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        setPlaceholder(new Label("No orders"));
    }

    public void setOrders(List<RepairOrder> orders) {
        getItems().setAll(orders);
    }
}