package com.fixmygadget.ui;

import com.fixmygadget.facade.FixMyGadgetFacade;
import com.fixmygadget.model.Customer;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomersPage extends Page {
    private final TableView<Customer> table = new TableView<>();
    private final TextField searchField = new TextField();
    private final Label countLabel = new Label();
    private List<Customer> all = new ArrayList<>();

    public CustomersPage(FixMyGadgetFacade facade) {
        super(facade, "Customers", "People who bring their gadgets for repair");

        searchField.setPromptText("Search by name or phone...");
        searchField.setPrefWidth(320);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> showFiltered());

        Button addButton = Ui.button("+ Add customer", "primary-button");
        addButton.setOnAction(e -> Ui.run(this::addCustomer));
        Button editButton = Ui.button("Edit", "secondary-button");
        editButton.setOnAction(e -> Ui.run(this::editCustomer));
        Button deleteButton = Ui.button("Delete", "danger-button");
        deleteButton.setOnAction(e -> Ui.run(this::deleteCustomer));
        Button historyButton = Ui.button("Repair history", "secondary-button");
        historyButton.setOnAction(e -> Ui.run(this::showHistory));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox actions = new HBox(10, searchField, spacer, addButton, editButton, deleteButton, historyButton);

        table.getColumns().add(Ui.textColumn("#", 70, c -> String.valueOf(c.getId())));
        table.getColumns().add(Ui.textColumn("Last name", 220, c -> c.getLastName()));
        table.getColumns().add(Ui.textColumn("First name", 220, c -> c.getFirstName()));
        table.getColumns().add(Ui.textColumn("Phone", 200, c -> c.getPhone()));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No customers yet"));
        VBox_grow();

        countLabel.getStyleClass().add("count-label");
        getChildren().addAll(actions, table, countLabel);
    }

    private void VBox_grow() {
        javafx.scene.layout.VBox.setVgrow(table, Priority.ALWAYS);
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
        new HistoryDialog(Ui.customerName(customer), facade.getCustomerHistory(customer.getId())).showAndWait();
    }
}