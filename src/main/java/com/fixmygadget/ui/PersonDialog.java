package com.fixmygadget.ui;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class PersonDialog extends Dialog<ButtonType> {
    private final TextField lastNameField = new TextField();
    private final TextField firstNameField = new TextField();
    private final TextField phoneField = new TextField();

    public PersonDialog(String title, boolean withPhone) {
        setTitle(title);
        setHeaderText(title);
        phoneField.setPromptText("+380...");

        GridPane grid = Ui.form();
        Ui.addRow(grid, 0, "Last name", lastNameField);
        Ui.addRow(grid, 1, "First name", firstNameField);
        if (withPhone) {
            Ui.addRow(grid, 2, "Phone", phoneField);
        }

        getDialogPane().setContent(grid);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Ui.style(this);
    }

    public void fill(String lastName, String firstName, String phone) {
        lastNameField.setText(lastName);
        firstNameField.setText(firstName);
        phoneField.setText(phone);
    }

    public String getLastName() { return lastNameField.getText().trim(); }
    public String getFirstName() { return firstNameField.getText().trim(); }
    public String getPhone() { return phoneField.getText().trim(); }
}