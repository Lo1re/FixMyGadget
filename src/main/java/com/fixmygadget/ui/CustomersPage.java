package com.fixmygadget.ui;

import com.fixmygadget.facade.FixMyGadgetFacade;
import com.fixmygadget.model.Customer;
import com.fixmygadget.sort.CustomerSortField;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class CustomersPage extends Page {
    private final TableView<Customer> table = new TableView<>();
    private final Map<CustomerSortField, Label> headers = new HashMap<>();
    private final TextField searchField = new TextField();
    private final Label countLabel = new Label();

    private List<Customer> all = new ArrayList<>();
    private CustomerSortField sortField = null;
    private boolean ascending = true;

    public CustomersPage(FixMyGadgetFacade facade) {
        super(facade, "Customers", "People who bring their gadgets for repair");

        searchField.setPromptText("Search by name or phone...");
        searchField.setPrefWidth(320);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> showFiltered());

        buildTable();
        countLabel.getStyleClass().add("count-label");
        getChildren().addAll(buildActions(), table, countLabel);
    }

    private HBox buildActions() {
        Button addButton = Ui.button("+ Add customer", "primary-button");
        addButton.setOnAction(e -> Ui.run(this::addCustomer));
        Button editButton = Ui.button("Edit", "secondary-button");
        editButton.setOnAction(e -> Ui.run(this::editCustomer));
        Button deleteButton = Ui.button("Delete", "danger-button");
        deleteButton.setOnAction(e -> Ui.run(this::deleteCustomer));
        Button historyButton = Ui.button("Repair history", "secondary-button");
        historyButton.setOnAction(e -> Ui.run(this::showHistory));

        HBox actions = new HBox(12, searchField, Ui.spacer(),
                Ui.buttonGroup(addButton, editButton, deleteButton),
                Ui.buttonGroup(historyButton));
        actions.setAlignment(Pos.CENTER_LEFT);
        return actions;
    }

    private void buildTable() {
        TableColumn<Customer, String> idColumn = Ui.textColumn("#", 70, c -> String.valueOf(c.getId()));
        TableColumn<Customer, String> lastNameColumn =
                sortable(CustomerSortField.LAST_NAME, 220, c -> c.getLastName());
        TableColumn<Customer, String> firstNameColumn =
                sortable(CustomerSortField.FIRST_NAME, 220, c -> c.getFirstName());
        TableColumn<Customer, String> phoneColumn =
                sortable(CustomerSortField.PHONE, 200, c -> c.getPhone());

        table.getColumns().addAll(idColumn, lastNameColumn, firstNameColumn, phoneColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No customers yet"));
        VBox.setVgrow(table, Priority.ALWAYS);
    }

    private TableColumn<Customer, String> sortable(CustomerSortField field, double width,
                                                   Function<Customer, String> getter) {
        TableColumn<Customer, String> column = Ui.textColumn(null, width, getter);
        Label header = new Label(field.getLabel());
        header.getStyleClass().add("sort-header");
        header.setOnMouseClicked(e -> Ui.run(() -> sortBy(field)));
        column.setGraphic(header);
        headers.put(field, header);
        return column;
    }

    private void sortBy(CustomerSortField field) {
        if (sortField == field) {
            ascending = !ascending;
        } else {
            sortField = field;
            ascending = true;
        }
        for (CustomerSortField f : CustomerSortField.values()) {
            Label header = headers.get(f);
            String arrow = "";
            if (f == sortField) {
                arrow = ascending ? "  ▲" : "  ▼";
            }
            header.setText(f.getLabel() + arrow);
        }
        showFiltered();
    }

    @Override
    public void refresh() {
        searchField.clear();
        all = facade.getAllCustomers();
        showFiltered();
    }

    private void showFiltered() {
        String text = searchField.getText().trim().toLowerCase();
        List<Customer> shown = new ArrayList<>();
        for (Customer customer : all) {
            String line = (customer.getLastName() + " " + customer.getFirstName()
                    + " " + customer.getPhone()).toLowerCase();
            if (line.contains(text)) {
                shown.add(customer);
            }
        }
        if (sortField != null) {
            facade.sortCustomers(shown, sortField, ascending);   // Quick sort в Java
        }
        table.getItems().setAll(shown);
        countLabel.setText(shown.size() + " customers");
    }

    private Customer selected() {
        Customer customer = table.getSelectionModel().getSelectedItem();
        if (customer == null) {
            throw new IllegalArgumentException("Select a customer in the table first");
        }
        return customer;
    }

    private void addCustomer() {
        PersonDialog dialog = new PersonDialog("New customer", true);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            facade.addCustomer(new Customer(dialog.getLastName(), dialog.getFirstName(), dialog.getPhone()));
            refresh();
        }
    }

    private void editCustomer() {
        Customer customer = selected();
        PersonDialog dialog = new PersonDialog("Edit customer", true);
        dialog.fill(customer.getLastName(), customer.getFirstName(), customer.getPhone());
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Customer updated = new Customer(dialog.getLastName(), dialog.getFirstName(), dialog.getPhone());
            updated.setId(customer.getId());
            facade.updateCustomer(updated);
            refresh();
        }
    }

    private void deleteCustomer() {
        Customer customer = selected();
        if (Ui.confirm("Delete customer " + Ui.customerName(customer) + "?")) {
            facade.deleteCustomer(customer.getId());
            refresh();
        }
    }

    private void showHistory() {
        Customer customer = selected();
        new HistoryDialog(facade, Ui.customerName(customer),
                facade.getCustomerHistory(customer.getId())).showAndWait();
    }
}