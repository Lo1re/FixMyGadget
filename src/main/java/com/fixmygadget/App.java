package com.fixmygadget;

import com.fixmygadget.ui.MainApp;
import javafx.application.Application;

public class App {

    public static void main(String[] args) {
        // Database initialization and wiring now happen behind the splash screen (see StartupTask).
        Application.launch(MainApp.class, args);
    }
}