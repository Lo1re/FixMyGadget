package com.fixmygadget.ui;

import com.fixmygadget.model.Feature;
import com.fixmygadget.model.Gadget;
import com.fixmygadget.model.GadgetBuilder;
import com.fixmygadget.model.GadgetType;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.EnumMap;
import java.util.Map;

public class GadgetForm extends Dialog<Gadget> {
    private final TextField nameField = new TextField();
    private final TextField manufacturerField = new TextField();
    private final TextField priceField = new TextField();
    private final ComboBox<GadgetType> typeBox = Ui.combo("Choose type");
    private final TextField widthField = new TextField();
    private final TextField heightField = new TextField();
    private final Map<Feature, CheckBox> featureBoxes = new EnumMap<>(Feature.class);

    private Gadget result;

    public GadgetForm(Gadget gadget) {
        setTitle(gadget == null ? "New gadget" : "Edit gadget");
        setHeaderText(gadget == null ? "Add a gadget to the catalog" : "Edit " + gadget.getName());

        typeBox.getItems().setAll(GadgetType.values());
        priceField.setPromptText("0.00");
        widthField.setPromptText("Width");
        heightField.setPromptText("Height");
        widthField.setPrefWidth(100);
        heightField.setPrefWidth(100);

        HBox screenRow = new HBox(10, widthField, heightField);
        HBox featuresRow = new HBox(16);
        featuresRow.setPadding(new Insets(4, 0, 0, 0));
        for (Feature feature : Feature.values()) {
            CheckBox box = new CheckBox(feature.getLabel());
            featureBoxes.put(feature, box);
            featuresRow.getChildren().add(box);
        }

        GridPane grid = Ui.form();
        Ui.addRow(grid, 0, "Name", nameField);
        Ui.addRow(grid, 1, "Manufacturer", manufacturerField);
        Ui.addRow(grid, 2, "Price (₴)", priceField);
        Ui.addRow(grid, 3, "Type", typeBox);
        Ui.addRow(grid, 4, "Screen (W × H)", screenRow);
        Ui.addRow(grid, 5, "Features", featuresRow);

        getDialogPane().setContent(grid);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Ui.style(this);

        if (gadget != null) {
            fill(gadget);
        }

        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                result = buildGadget(gadget);
            } catch (IllegalArgumentException | IllegalStateException e) {
                Ui.showError("Check the form", e.getMessage());
                event.consume();   // вікно лишається відкритим
            }
        });
        setResultConverter(button -> button == ButtonType.OK ? result : null);
    }

    private void fill(Gadget gadget) {
        nameField.setText(gadget.getName());
        manufacturerField.setText(gadget.getManufacturer());
        priceField.setText(gadget.getPrice().toPlainString());
        typeBox.setValue(gadget.getType());
        widthField.setText(String.valueOf(gadget.getScreenWidth()));
        heightField.setText(String.valueOf(gadget.getScreenHeight()));
        for (Feature feature : Feature.values()) {
            featureBoxes.get(feature).setSelected(gadget.hasFeature(feature));
        }
    }

    private Gadget buildGadget(Gadget original) {
        String name = nameField.getText().trim();
        String manufacturer = manufacturerField.getText().trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (manufacturer.isEmpty()) {
            throw new IllegalArgumentException("Manufacturer is required");
        }

        GadgetBuilder builder = new GadgetBuilder()
                .name(name)
                .manufacturer(manufacturer)
                .price(Ui.parseMoney(priceField.getText(), "Price"))
                .type(typeBox.getValue())
                .screen(Ui.parseInt(widthField.getText(), "Screen width"),
                        Ui.parseInt(heightField.getText(), "Screen height"));
        if (original != null) {
            builder.id(original.getId());
        }
        for (Feature feature : Feature.values()) {
            if (featureBoxes.get(feature).isSelected()) {
                builder.feature(feature);
            }
        }
        return builder.build();
    }
}