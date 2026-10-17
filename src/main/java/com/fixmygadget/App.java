package com.fixmygadget;

import com.fixmygadget.db.DatabaseInitializer;
import com.fixmygadget.ui.MainApp;
import javafx.application.Application;

public class App {
    public static void main(String[] args) {
        new DatabaseInitializer().init();
        Application.launch(MainApp.class, args);
    }
}