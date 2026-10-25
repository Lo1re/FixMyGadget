package com.fixmygadget.ui;

import com.fixmygadget.facade.FixMyGadgetFacade;
import com.fixmygadget.model.Feature;
import com.fixmygadget.model.Gadget;
import com.fixmygadget.model.GadgetType;
import com.fixmygadget.sort.GadgetSortField;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class GadgetsPage extends Page {
    private static final Logger log = LoggerFactory.getLogger(GadgetsPage.class);

    private final TableView<Gadget> table = new TableView<>();
    private final Map<GadgetSortField, Label> headers = new HashMap<>();
    private final Label countLabel = new Label();

    private final TextField nameField = new TextField();
    private final ComboBox<GadgetType> typeBox = Ui.combo("Any type");
    private final ComboBox<String> manufacturerBox = Ui.combo("Manufacturer");
    private final TextField x1Field = new TextField();
    private final TextField y1Field = new TextField();
    private final TextField x2Field = new TextField();
    private final TextField y2Field = new TextField();

    private List<Gadget> current = new ArrayList<>();
    private GadgetSortField sortField = null;
    private boolean ascending = true;

    public GadgetsPage(FixMyGadgetFacade facade) {
        super(facade, "Gadgets", "Catalog of smartphones, tablets and watches");

        nameField.setPromptText("Name contains...");
        typeBox.getItems().setAll(GadgetType.values());
        for (TextField field : new TextField[]{x1Field, y1Field, x2Field, y2Field}) {
            field.setPrefWidth(75);
        }
        x1Field.setPromptText("W");
        y1Field.setPromptText("H");
        x2Field.setPromptText("W");
        y2Field.setPromptText("H");

        buildTable();
        getChildren().addAll(buildFilters(), buildActions(), table, countLabel);
        countLabel.getStyleClass().add("count-label");
    }

    private VBox buildFilters() {
        Button searchButton = Ui.button("Search", "primary-button");
        searchButton.setOnAction(e -> Ui.run(this::search));

        Button manufacturerButton = Ui.button("Filter", "secondary-button");
        manufacturerButton.setOnAction(e -> Ui.run(this::filterByManufacturer));

        Button screenButton = Ui.button("Find", "secondary-button");
        screenButton.setOnAction(e -> Ui.run(this::filterByScreen));

        Button resetButton = Ui.button("Reset all", "secondary-button");
        resetButton.setOnAction(e -> Ui.run(this::refresh));

        FlowPane row1 = new FlowPane(10, 10,
                nameField, typeBox, searchButton, label("   Manufacturer"), manufacturerBox, manufacturerButton);
        FlowPane row2 = new FlowPane(10, 10,
                label("Screen from"), x1Field, label("×"), y1Field,
                label("to"), x2Field, label("×"), y2Field, screenButton, resetButton);

        Label title = new Label("Search and filters");
        title.getStyleClass().add("section-title");

        VBox card = new VBox(12, title, row1, row2);
        card.getStyleClass().add("card");
        return card;
    }

    private HBox buildActions() {
        Button addButton = Ui.button("+ Add gadget", "primary-button");
        addButton.setOnAction(e -> Ui.run(this::addGadget));

        Button editButton = Ui.button("Edit", "secondary-button");
        editButton.setOnAction(e -> Ui.run(this::editGadget));

        Button deleteButton = Ui.button("Delete", "danger-button");
        deleteButton.setOnAction(e -> Ui.run(this::deleteGadget));

        Button addFeatureButton = Ui.button("+ Feature", "secondary-button");
        addFeatureButton.setOnAction(e -> Ui.run(() -> changeFeature(true)));

        Button removeFeatureButton = Ui.button("− Feature", "secondary-button");
        removeFeatureButton.setOnAction(e -> Ui.run(() -> changeFeature(false)));

        Button historyButton = Ui.button("Repair history", "secondary-button");
        historyButton.setOnAction(e -> Ui.run(this::showHistory));

        Button importButton = Ui.button("Import", "secondary-button");
        importButton.setOnAction(e -> Ui.run(this::importCatalog));

        Button exportButton = Ui.button("Export", "secondary-button");
        exportButton.setOnAction(e -> Ui.run(this::exportCatalog));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        return new HBox(10, addButton, editButton, deleteButton, addFeatureButton,
                removeFeatureButton, historyButton, spacer, importButton, exportButton);
    }

    private Label label(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("field-label");
        return label;
    }

    private void buildTable() {
        TableColumn<Gadget, String> idColumn = Ui.textColumn("#", 60, g -> String.valueOf(g.getId()));
        TableColumn<Gadget, String> nameColumn = sortable(GadgetSortField.NAME, 220, g -> g.getName());
        TableColumn<Gadget, String> manufacturerColumn =
                sortable(GadgetSortField.MANUFACTURER, 150, g -> g.getManufacturer());
        TableColumn<Gadget, String> typeColumn =
                sortable(GadgetSortField.TYPE, 120, g -> g.getType().getLabel());
        TableColumn<Gadget, String> priceColumn =
                sortable(GadgetSortField.PRICE, 120, g -> Ui.money(g.getPrice()));
        TableColumn<Gadget, String> widthColumn =
                sortable(GadgetSortField.SCREEN_WIDTH, 110, g -> String.valueOf(g.getScreenWidth()));
        TableColumn<Gadget, String> heightColumn =
                sortable(GadgetSortField.SCREEN_HEIGHT, 110, g -> String.valueOf(g.getScreenHeight()));
        TableColumn<Gadget, String> featuresColumn =
                Ui.textColumn("Features", 240, g -> featuresText(g));

        table.getColumns().addAll(idColumn, nameColumn, manufacturerColumn, typeColumn,
                priceColumn, widthColumn, heightColumn, featuresColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No gadgets found"));
        VBox.setVgrow(table, Priority.ALWAYS);
    }

    private TableColumn<Gadget, String> sortable(GadgetSortField field, double width,
                                                 Function<Gadget, String> getter) {
        TableColumn<Gadget, String> column = Ui.textColumn(null, width, getter);
        Label header = new Label(field.getLabel());
        header.getStyleClass().add("sort-header");
        header.setOnMouseClicked(e -> Ui.run(() -> sortBy(field)));
        column.setGraphic(header);
        headers.put(field, header);
        return column;
    }

    private String featuresText(Gadget gadget) {
        String text = "";
        for (Feature feature : gadget.getFeatures()) {
            if (!text.isEmpty()) {
                text += ", ";
            }
            text += feature.getLabel();
        }
        if (text.isEmpty()) {
            return "—";
        }
        return text;
    }


    private void sortBy(GadgetSortField field) {
        if (sortField == field) {
            ascending = !ascending;
        } else {
            sortField = field;
            ascending = true;
        }
        for (GadgetSortField f : GadgetSortField.values()) {
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
            facade.sortGadgets(current, sortField, ascending);   // Quick sort в Java
        }
        table.getItems().setAll(current);
        countLabel.setText(current.size() + " gadgets");
    }


    @Override
    public void refresh() {
        nameField.clear();
        typeBox.setValue(null);
        manufacturerBox.setValue(null);
        x1Field.clear();
        y1Field.clear();
        x2Field.clear();
        y2Field.clear();

        List<Gadget> all = facade.getAllGadgets();
        List<String> manufacturers = new ArrayList<>();
        for (Gadget gadget : all) {
            if (!manufacturers.contains(gadget.getManufacturer())) {
                manufacturers.add(gadget.getManufacturer());
            }
        }
        manufacturerBox.getItems().setAll(manufacturers);

        current = all;
        showCurrent();
    }

    private void search() {
        String name = nameField.getText().trim();
        GadgetType type = typeBox.getValue();

        if (name.isEmpty() && type == null) {
            current = facade.getAllGadgets();
        } else if (name.isEmpty()) {
            current = facade.findGadgetsByType(type);
        } else if (type != null) {
            current = facade.findGadgets(name, type);
        } else {
            current = new ArrayList<>();
            for (Gadget gadget : facade.getAllGadgets()) {
                if (gadget.getName().toLowerCase().contains(name.toLowerCase())) {
                    current.add(gadget);
                }
            }
        }
        showCurrent();
    }

    private void filterByManufacturer() {
        String manufacturer = manufacturerBox.getValue();
        if (manufacturer == null) {
            throw new IllegalArgumentException("Choose a manufacturer first");
        }
        current = facade.findGadgetsByManufacturer(manufacturer);
        showCurrent();
    }

    private void filterByScreen() {
        int x1 = Ui.parseInt(x1Field.getText(), "Screen width (from)");
        int y1 = Ui.parseInt(y1Field.getText(), "Screen height (from)");
        int x2 = Ui.parseInt(x2Field.getText(), "Screen width (to)");
        int y2 = Ui.parseInt(y2Field.getText(), "Screen height (to)");
        if (x1 > x2 || y1 > y2) {
            throw new IllegalArgumentException("'From' size must not be bigger than 'to' size");
        }
        current = facade.findGadgetsByScreenSize(x1, y1, x2, y2);
        showCurrent();
    }



    private Gadget selected() {
        Gadget gadget = table.getSelectionModel().getSelectedItem();
        if (gadget == null) {
            throw new IllegalArgumentException("Select a gadget in the table first");
        }
        return gadget;
    }

    private void addGadget() {
        Optional<Gadget> result = new GadgetForm(null).showAndWait();
        if (result.isPresent()) {
            facade.addGadget(result.get());
            refresh();
        }
    }

    private void editGadget() {
        Gadget gadget = selected();
        Optional<Gadget> result = new GadgetForm(gadget).showAndWait();
        if (result.isPresent()) {
            facade.updateGadget(result.get());
            refresh();
        }
    }

    private void deleteGadget() {
        Gadget gadget = selected();
        if (Ui.confirm("Delete \"" + gadget.getName() + "\" from the catalog?")) {
            facade.deleteGadget(gadget.getId());
            refresh();
        }
    }

    private void changeFeature(boolean add) {
        Gadget gadget = selected();
        ChoiceDialog<Feature> dialog = new ChoiceDialog<>(Feature.CAMERA, Feature.values());
        dialog.setTitle(add ? "Add feature" : "Remove feature");
        dialog.setHeaderText(gadget.getName());
        dialog.setContentText("Feature:");
        Ui.style(dialog);

        Optional<Feature> result = dialog.showAndWait();
        if (result.isPresent()) {
            if (add) {
                facade.addFeature(gadget.getId(), result.get());
            } else {
                facade.removeFeature(gadget.getId(), result.get());
            }
            refresh();
        }
    }

    private void showHistory() {
        Gadget gadget = selected();
        new HistoryDialog(gadget.getName(), facade.getGadgetHistory(gadget.getId())).showAndWait();
    }


    private void importCatalog() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import catalog");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*.txt"));
        File file = chooser.showOpenDialog(getScene().getWindow());
        if (file == null) {
            return;
        }
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            int count = facade.importCatalog(reader);
            Ui.showInfo("Import finished", count + " gadgets imported");
        } catch (IOException e) {
            log.error("Cannot read file", e);
            Ui.showError("File error", "Cannot read the file");
        } finally {
            refresh();
        }
    }

    private void exportCatalog() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export catalog");
        chooser.setInitialFileName("gadgets.txt");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*.txt"));
        File file = chooser.showSaveDialog(getScene().getWindow());
        if (file == null) {
            return;
        }
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            facade.exportCatalog(writer);
            Ui.showInfo("Export finished", "Catalog saved to " + file.getName());
        } catch (IOException e) {
            log.error("Cannot write file", e);
            Ui.showError("File error", "Cannot write the file");
        }
    }
}