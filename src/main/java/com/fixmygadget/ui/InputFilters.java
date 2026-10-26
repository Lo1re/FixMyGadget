package com.fixmygadget.ui;

import com.fixmygadget.util.InputRules;
import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

/** Limits what the user can type into a text field. Wrong characters are simply not entered. */
public final class InputFilters {

    private InputFilters() {
    }

    /** Only letters (any language), space, hyphen and apostrophe. */
    public static void nameOnly(TextField field) {
        field.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches(InputRules.NAME_CHAR + "*") ? change : null));
    }

    /** Phone is always "+380" followed by up to 9 digits. The "+380" part cannot be deleted. */
    public static void phoneOnly(TextField field) {
        field.setTextFormatter(new TextFormatter<String>(change -> {
            String text = change.getControlNewText();
            boolean ok = text.startsWith(InputRules.PHONE_PREFIX)
                    && text.length() <= InputRules.PHONE_LENGTH
                    && text.substring(InputRules.PHONE_PREFIX.length()).matches("\\d*");
            return ok ? change : null;
        }));
        field.setText(InputRules.PHONE_PREFIX);

        // when the field gets focus, put the cursor at the end (so typing does not replace "+380")
        field.focusedProperty().addListener((observable, wasFocused, isFocused) -> {
            if (isFocused) {
                Platform.runLater(() -> field.positionCaret(field.getLength()));
            }
        });
    }

    /** Only digits. */
    public static void digitsOnly(TextField field) {
        field.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d*") ? change : null));
    }

    /** A positive number with at most two digits after the dot or comma. */
    public static void moneyOnly(TextField field) {
        field.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d*([.,]\\d{0,2})?") ? change : null));
    }
}