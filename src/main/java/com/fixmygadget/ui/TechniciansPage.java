package com.fixmygadget.ui;

import com.fixmygadget.facade.FixMyGadgetFacade;
import com.fixmygadget.model.Technician;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

public class TechniciansPage extends Page {
    private final TableView<Technician> table = new TableView<>();
    private final Label countLabel = new Label();

    public TechniciansPage(FixMyGadgetFacade facade) {
        super(facade, "Technicians", "Masters who repair the gadgets");

        Button addButton = Ui.button("+ Add technician", "primary-button");
        addButton.setOnAction(e -> Ui.run(this::addTechnician));
        Button editButton = Ui.button("Edit", "secondary-button");
        editButton.setOnAction(e -> Ui.run(this::editTechnician));
        Button deleteButton = Ui.button("Delete", "danger-button");
        deleteButton.setOnAction(e -> Ui.run(this::deleteTechnician));
        Button ordersButton = Ui.button("Assigned orders", "secondary-button");
        ordersButton.setOnAction(e -> Ui.run(this::showOrders));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox actions = new HBox(10, addButton, editButton, deleteButton, spacer, ordersButton);

        table.getColumns().add(Ui.textColumn("#", 70, t -> String.valueOf(t.getId())));
        table.getColumns().add(Ui.textColumn("Last name", 260, t -> t.getLastName()));
        table.getColumns().add(Ui.textColumn("First name", 260, t -> t.getFirstName()));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No technicians yet"));
        VBox.setVgrow(table, Priority.ALWAYS);

        countLabel.getStyleClass().add("count-label");
        getChildren().addAll(actions, table, countLabel);
    }

    @Override
    public void refresh() {
        List<Technician> technicians = facade.getAllTechnicians();
        table.getItems().setAll(technicians);
        countLabel.setText(technicians.size() + " technicians");
    }

    private Technician selected() {
        Technician technician = table.getSelectionModel().getSelectedItem();
        if (technician == null) {
            throw new IllegalArgumentException("Select a technician in the table first");
        }
        return technician;
    }

    private void addTechnician() {
        PersonDialog dialog = new PersonDialog("New technician", false);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            facade.addTechnician(new Technician(dialog.getLastName(), dialog.getFirstName()));
            refresh();
        }
    }

    private void editTechnician() {
        Technician technician = selected();
        PersonDialog dialog = new PersonDialog("Edit technician", false);
        dialog.fill(technician.getLastName(), technician.getFirstName(), "");
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Technician updated = new Technician(dialog.getLastName(), dialog.getFirstName());
            updated.setId(technician.getId());
            facade.updateTechnician(updated);
            refresh();
        }
    }

    private void deleteTechnician() {
        Technician technician = selected();
        if (Ui.confirm("Delete technician " + Ui.technicianName(technician) + "?")) {
            facade.deleteTechnician(technician.getId());
            refresh();
        }
    }

    private void showOrders() {
        Technician technician = selected();
        new HistoryDialog(Ui.technicianName(technician),
                facade.findOrdersByTechnician(technician.getId())).showAndWait();
    }
}