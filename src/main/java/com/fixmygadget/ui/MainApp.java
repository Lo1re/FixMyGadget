package com.fixmygadget.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) {
        stage.setTitle("FixMyGadget");
        stage.setScene(new Scene(new Label("FixMyGadget працює"), 400, 200));
        stage.show();
    }
}