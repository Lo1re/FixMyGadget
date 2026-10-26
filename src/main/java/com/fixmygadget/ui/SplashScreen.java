package com.fixmygadget.ui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/** Start-up window: company name on a gradient background and a progress bar. */
public class SplashScreen {
    private static final double BAR_WIDTH = 380;
    private static final double BAR_HEIGHT = 8;

    private final Stage stage = new Stage(StageStyle.TRANSPARENT);
    private final DoubleProperty progress = new SimpleDoubleProperty(0);
    private final Label status = new Label("Starting...");
    private final Label percent = new Label("0%");

    public SplashScreen() {
        Label company = new Label("RBK Solutions");
        company.setStyle("-fx-font-size: 52px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label product = new Label("FixMyGadget  \u00B7  Repair service CRM");
        product.setStyle("-fx-font-size: 15px; -fx-text-fill: rgba(255,255,255,0.8);");

        // progress bar built from two regions: a track and a fill that grows with the progress
        Region fill = new Region();
        fill.setMinHeight(BAR_HEIGHT);
        fill.setPrefHeight(BAR_HEIGHT);
        fill.setMaxHeight(BAR_HEIGHT);
        fill.setMinWidth(0);
        fill.setMaxWidth(Region.USE_PREF_SIZE);
        fill.setStyle("-fx-background-color: white; -fx-background-radius: 6;");

        StackPane track = new StackPane(fill);
        track.setAlignment(Pos.CENTER_LEFT);
        track.setMinSize(BAR_WIDTH, BAR_HEIGHT);
        track.setPrefSize(BAR_WIDTH, BAR_HEIGHT);
        track.setMaxSize(BAR_WIDTH, BAR_HEIGHT);
        track.setStyle("-fx-background-color: rgba(255,255,255,0.28); -fx-background-radius: 6;");
        fill.prefWidthProperty().bind(track.widthProperty().multiply(progress));

        String smallText = "-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.8);";
        status.setStyle(smallText);
        percent.setStyle(smallText);
        Region grow = new Region();
        HBox.setHgrow(grow, Priority.ALWAYS);
        HBox statusRow = new HBox(status, grow, percent);
        statusRow.setMaxWidth(BAR_WIDTH);

        Region space = new Region();
        space.setMinHeight(46);

        VBox card = new VBox(8, company, product, space, track, statusRow);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(640, 360);
        card.setMinSize(640, 360);
        card.setMaxSize(640, 360);
        card.setStyle("-fx-background-color: linear-gradient(to bottom right, #4f46e5, #7c3aed 60%, #db2777);"
                + "-fx-background-radius: 22;");
        card.setEffect(new DropShadow(26, Color.rgb(15, 23, 42, 0.4)));

        StackPane root = new StackPane(card);
        root.setPadding(new Insets(28));
        root.setStyle("-fx-background-color: transparent;");

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.setTitle("FixMyGadget");
    }

    /** Shows the task's progress and messages in this window. */
    public void bind(Task<?> task) {
        task.progressProperty().addListener((o, oldValue, newValue) -> setProgress(newValue.doubleValue()));
        task.messageProperty().addListener((o, oldValue, newValue) -> status.setText(newValue));
    }

    private void setProgress(double value) {
        double shown = Math.max(0, Math.min(1, value));   // Task reports -1 before the first update
        progress.set(shown);
        percent.setText(Math.round(shown * 100) + "%");
    }

    public void show() {
        stage.show();
        stage.centerOnScreen();
    }

    public void close() {
        stage.close();
    }
}