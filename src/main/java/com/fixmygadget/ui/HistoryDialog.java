package com.fixmygadget.ui;

import com.fixmygadget.model.RepairOrder;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import java.util.List;

public class HistoryDialog extends Dialog<Void> {

    public HistoryDialog(String title, List<RepairOrder> orders) {
        setTitle("Repair history");
        setHeaderText(title + "  (" + orders.size() + " orders)");

        OrdersTable table = new OrdersTable();
        table.setOrders(orders);
        table.setPrefSize(1000, 380);

        getDialogPane().setContent(table);
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        setResizable(true);
        Ui.style(this);
    }
}