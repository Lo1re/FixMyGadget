package com.fixmygadget.ui;

import com.fixmygadget.facade.FixMyGadgetFacade;
import com.fixmygadget.model.Technician;
import com.fixmygadget.sort.TechnicianSortField;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class TechniciansPage extends Page {
    private final TableView<Technician> table = new TableView<>();
    private final Map<TechnicianSortField, Label> headers = new HashMap<>();
    private final Label countLabel = new Label();

    private List<Technician> current = List.of();
    private TechnicianSortField sortField = null;
    private boolean ascending = true;

    public TechniciansPage(FixMyGadgetFacade facade) {
        super(facade, "Technicians", "Masters who repair the gadgets");

        buildTable();
        countLabel.getStyleClass().add("count-label");
        getChildren().addAll(buildActions(), table, countLabel);
    }

    private HBox buildActions() {
        Button addButton = Ui.button("+ Add technician", "primary-button");
        addButton.setOnAction(e -> Ui.run(this::addTechnician));
        Button editButton = Ui.button("Edit", "secondary-button");
        editButton.setOnAction(e -> Ui.run(this::editTechnician));
        Button deleteButton = Ui.button("Delete", "danger-button");
        deleteButton.setOnAction(e -> Ui.run(this::deleteTechnician));
        Button ordersButton = Ui.button("Assigned orders", "secondary-button");
        ordersButton.setOnAction(e -> Ui.run(this::showOrders));

        HBox actions = new HBox(12,
                Ui.buttonGroup(addButton, editButton, deleteButton),
                Ui.spacer(),
                Ui.buttonGroup(ordersButton));
        actions.setAlignment(Pos.CENTER_LEFT);
        return actions;
    }

    private void buildTable() {
        TableColumn<Technician, String> idColumn = Ui.textColumn("#", 70, t -> String.valueOf(t.getId()));
        TableColumn<Technician, String> lastNameColumn =
                sortable(TechnicianSortField.LAST_NAME, 260, t -> t.getLastName());
        TableColumn<Technician, String> firstNameColumn =
                sortable(TechnicianSortField.FIRST_NAME, 260, t -> t.getFirstName());

        table.getColumns().addAll(idColumn, lastNameColumn, firstNameColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No technicians yet"));
        VBox.setVgrow(table, Priority.ALWAYS);
    }

    private TableColumn<Technician, String> sortable(TechnicianSortField field, double width,
                                                     Function<Technician, String> getter) {
        TableColumn<Technician, String> column = Ui.textColumn(null, width, getter);
        Label header = new Label(field.getLabel());
        header.getStyleClass().add("sort-header");
        header.setOnMouseClicked(e -> Ui.run(() -> sortBy(field)));
        column.setGraphic(header);
        headers.put(field, header);
        return column;
    }

    private void sortBy(TechnicianSortField field) {
        if (sortField == field) {
            ascending = !ascending;
        } else {
            sortField = field;
            ascending = true;
        }
        for (TechnicianSortField f : TechnicianSortField.values()) {
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
            facade.sortTechnicians(current, sortField, ascending);   // Quick sort в Java
        }
        table.getItems().setAll(current);
        countLabel.setText(current.size() + " technicians");
    }

    @Override
    public void refresh() {
        current = facade.getAllTechnicians();
        showCurrent();
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
        new HistoryDialog(facade, Ui.technicianName(technician),
                facade.findOrdersByTechnician(technician.getId())).showAndWait();
    }
}