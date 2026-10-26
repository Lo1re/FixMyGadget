package com.fixmygadget.ui;

import com.fixmygadget.util.InputRules;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class PersonDialog extends Dialog<ButtonType> {
    private final TextField lastNameField = new TextField();
    private final TextField firstNameField = new TextField();
    private final TextField phoneField = new TextField();
    private final Label errorLabel = new Label();
    private final boolean withPhone;

    public PersonDialog(String title, boolean withPhone) {
        this.withPhone = withPhone;
        setTitle(title);
        setHeaderText(title);

        InputFilters.nameOnly(lastNameField);
        InputFilters.nameOnly(firstNameField);

        GridPane grid = Ui.form();
        Ui.addRow(grid, 0, "Last name", lastNameField);
        Ui.addRow(grid, 1, "First name", firstNameField);
        if (withPhone) {
            InputFilters.phoneOnly(phoneField);
            Ui.addRow(grid, 2, "Phone", phoneField);
        }
        errorLabel.getStyleClass().add("error-label");
        grid.add(errorLabel, 0, withPhone ? 3 : 2, 2, 1);

        getDialogPane().setContent(grid);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Ui.style(this);

        // do not close the dialog while the data is wrong - show the reason inside the dialog
        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            String error = findError();
            if (error != null) {
                errorLabel.setText(error);
                event.consume();
            }
        });
    }

    public void fill(String lastName, String firstName, String phone) {
        lastNameField.setText(lastName);
        firstNameField.setText(firstName);
        if (withPhone) {
            phoneField.setText(phone);
        }
    }

    private String findError() {
        if (!InputRules.isValidName(getLastName())) {
            return "Last name is required and must contain only letters";
        }
        if (!InputRules.isValidName(getFirstName())) {
            return "First name is required and must contain only letters";
        }
        if (withPhone && !InputRules.isValidPhone(getPhone())) {
            return "Phone must be +380 and 9 digits";
        }
        return null;
    }

    public String getLastName() { return lastNameField.getText().trim(); }
    public String getFirstName() { return firstNameField.getText().trim(); }
    public String getPhone() { return phoneField.getText().trim(); }
}