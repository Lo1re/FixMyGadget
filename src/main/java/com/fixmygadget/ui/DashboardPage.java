package com.fixmygadget.ui;

import com.fixmygadget.facade.FixMyGadgetFacade;
import com.fixmygadget.model.RepairOrder;
import com.fixmygadget.model.RepairStatus;
import com.fixmygadget.sort.RepairOrderSortField;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DashboardPage extends Page {
    private final Label gadgetsValue = new Label("0");
    private final Label customersValue = new Label("0");
    private final Label techniciansValue = new Label("0");
    private final Label activeValue = new Label("0");
    private final Label revenueValue = new Label("0");
    private final OrdersTable recentTable = new OrdersTable(facade);

    public DashboardPage(FixMyGadgetFacade facade) {
        super(facade, "Dashboard", "Overview of the service center");

        HBox cards = new HBox(18,
                statCard("📱", "icon-indigo", "Gadgets in catalog", gadgetsValue),
                statCard("👤", "icon-sky", "Customers", customersValue),
                statCard("🔧", "icon-violet", "Technicians", techniciansValue),
                statCard("⏳", "icon-amber", "Active orders", activeValue),
                statCard("💰", "icon-green", "Monthly revenue", revenueValue));

        Label recentTitle = new Label("Latest orders");
        recentTitle.getStyleClass().add("section-title");

        VBox.setVgrow(recentTable, Priority.ALWAYS);
        getChildren().addAll(cards, recentTitle, recentTable);
    }

    private static final double CARD_PADDING = 14;
    private static final double ICON_SIZE = 44;
    private static final double CARD_SPACING = 10;

    private HBox statCard(String icon, String iconStyle, String title, Label value) {
        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().addAll("stat-icon", iconStyle);
        iconLabel.setStyle("-fx-min-width: " + ICON_SIZE + "; -fx-min-height: " + ICON_SIZE
                + "; -fx-font-size: 20px;");
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-title");
        value.getStyleClass().add("stat-value");

        HBox card = new HBox(CARD_SPACING, iconLabel, new VBox(2, titleLabel, value));
        card.getStyleClass().add("card");
        card.setStyle("-fx-padding: " + CARD_PADDING + ";");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMinWidth(0);
        card.setPrefWidth(210);
        card.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(card, Priority.ALWAYS);

        // text shrinks to fit the card instead of being cut with "..."
        double reserved = 2 * CARD_PADDING + ICON_SIZE + CARD_SPACING + 4;
        Ui.fitFont(titleLabel, card, reserved, 13, 9, false);
        Ui.fitFont(value, card, reserved, 24, 12, true);
        return card;
    }

    @Override
    public void refresh() {
        List<RepairOrder> orders = facade.getAllOrders();

        int active = 0;
        for (RepairOrder order : orders) {
            if (order.getStatus() != RepairStatus.DONE) {
                active++;
            }
        }

        LocalDate today = LocalDate.now();
        gadgetsValue.setText(String.valueOf(facade.getAllGadgets().size()));
        customersValue.setText(String.valueOf(facade.getAllCustomers().size()));
        techniciansValue.setText(String.valueOf(facade.getAllTechnicians().size()));
        activeValue.setText(String.valueOf(active));
        revenueValue.setText(Ui.money(facade.getRevenue(today.withDayOfMonth(1), today)));

        List<RepairOrder> sorted = facade.sortOrders(orders, RepairOrderSortField.CREATED_AT, false);
        List<RepairOrder> recent = new ArrayList<>();
        for (int i = 0; i < sorted.size() && i < 6; i++) {
            recent.add(sorted.get(i));
        }
        recentTable.setOrders(recent);
    }
}