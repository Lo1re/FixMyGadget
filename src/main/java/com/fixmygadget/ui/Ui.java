package com.fixmygadget.ui;

import com.fixmygadget.exception.DaoException;
import com.fixmygadget.exception.InvalidStatusTransitionException;
import com.fixmygadget.exception.NotFoundException;
import com.fixmygadget.model.Customer;
import com.fixmygadget.model.RepairOrder;
import com.fixmygadget.model.RepairStatus;
import com.fixmygadget.model.Technician;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public final class Ui {
    private static final Logger log = LoggerFactory.getLogger(Ui.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public static final String CSS = Ui.class.getResource("/css/style.css").toExternalForm();

    private Ui() {
    }


    public static void run(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException | IllegalStateException
                 | NotFoundException | InvalidStatusTransitionException e) {
            showError("Cannot do this", e.getMessage());
        } catch (DaoException e) {
            log.error("Database error", e);
            String text = e.getMessage();
            if (e.getCause() != null) {
                text += "\n" + e.getCause().getMessage();
            }
            showError("Database error", text);
        } catch (RuntimeException e) {
            log.error("Unexpected error", e);
            showError("Unexpected error", String.valueOf(e.getMessage()));
        }
    }

    public static void showError(String header, String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("FixMyGadget");
        alert.setHeaderText(header);
        alert.setContentText(text);
        style(alert);
        alert.showAndWait();
    }

    public static void showInfo(String header, String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("FixMyGadget");
        alert.setHeaderText(header);
        alert.setContentText(text);
        style(alert);
        alert.showAndWait();
    }

    public static boolean confirm(String text) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, text, ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Please confirm");
        alert.setHeaderText(null);
        style(alert);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static void style(Dialog<?> dialog) {
        dialog.getDialogPane().getStylesheets().add(CSS);
    }


    public static int parseInt(String text, String field) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(field + " must be a whole number");
        }
    }

    public static BigDecimal parseMoney(String text, String field) {
        try {
            return new BigDecimal(text.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(field + " must be a number");
        }
    }



    public static String money(BigDecimal value) {
        if (value == null) {
            return "—";
        }
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString() + " ₴";
    }

    public static String date(LocalDateTime value) {
        if (value == null) {
            return "—";
        }
        return value.format(DATE_FORMAT);
    }

    public static String customerName(Customer customer) {
        return customer.getLastName() + " " + customer.getFirstName();
    }

    public static String technicianName(Technician technician) {
        if (technician == null) {
            return "—";
        }
        return technician.getLastName() + " " + technician.getFirstName();
    }



    public static Button button(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().add(styleClass);
        return button;
    }

    public static <T> ComboBox<T> combo(String prompt) {
        ComboBox<T> box = new ComboBox<>();
        box.setPromptText(prompt);
        box.setPrefWidth(190);
        box.setButtonCell(new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(prompt);
                } else {
                    setText(item.toString());
                }
            }
        });
        return box;
    }

    public static HBox buttonGroup(Node... buttons) {
        HBox group = new HBox(8, buttons);
        group.getStyleClass().add("button-group");
        group.setAlignment(Pos.CENTER_LEFT);
        return group;
    }

    public static Region spacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    public static VBox labeled(String text, Node field) {
        Label label = new Label(text);
        label.getStyleClass().add("field-label");
        return new VBox(5, label, field);
    }

    public static <T> List<T> intersect(List<T> first, List<T> second, Function<T, Integer> idOf) {
        Set<Integer> ids = new HashSet<>();
        for (T item : second) {
            ids.add(idOf.apply(item));
        }
        List<T> result = new ArrayList<>();
        for (T item : first) {
            if (ids.contains(idOf.apply(item))) {
                result.add(item);
            }
        }
        return result;
    }

    public static GridPane form() {
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(12);
        grid.setPadding(new Insets(10, 6, 6, 6));
        return grid;
    }

    public static void addRow(GridPane grid, int row, String label, Node field) {
        Label name = new Label(label);
        name.getStyleClass().add("field-label");
        grid.add(name, 0, row);
        grid.add(field, 1, row);
    }


    public static <T> TableColumn<T, String> textColumn(String title, double width, Function<T, String> getter) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setSortable(false);   // сортуємо самі, через QuickSort
        column.setCellValueFactory(data -> new SimpleStringProperty(getter.apply(data.getValue())));
        return column;
    }

    public static TableColumn<RepairOrder, RepairStatus> statusColumn(String title, double width) {
        TableColumn<RepairOrder, RepairStatus> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setSortable(false);
        column.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getStatus()));
        column.setCellFactory(col -> new TableCell<RepairOrder, RepairStatus>() {
            @Override
            protected void updateItem(RepairStatus status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(status.getLabel());
                String name = status.name().toLowerCase().replace('_', '-');
                badge.getStyleClass().addAll("badge", "badge-" + name);
                setGraphic(badge);
            }
        });
        return column;
    }
}