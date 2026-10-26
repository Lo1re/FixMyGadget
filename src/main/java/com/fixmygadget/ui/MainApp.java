package com.fixmygadget.ui;

import com.fixmygadget.facade.FixMyGadgetFacade;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MainApp extends Application {
    private static final Logger log = LoggerFactory.getLogger(MainApp.class);

    private static final double WIDTH = 1360;
    private static final double HEIGHT = 840;

    private static FixMyGadgetFacade facade;

    private final BorderPane root = new BorderPane();
    private final BorderPane content = new BorderPane();
    private final List<Page> pages = new ArrayList<>();
    private final List<Button> navButtons = new ArrayList<>();

    private double dragX;
    private double dragY;

    public static void setFacade(FixMyGadgetFacade facade) {
        MainApp.facade = facade;
    }

    @Override
    public void start(Stage stage) {
        Thread.setDefaultUncaughtExceptionHandler((thread, e) -> {
            log.error("Unexpected error", e);
            Platform.runLater(() -> Ui.showError("Unexpected error", String.valueOf(e.getMessage())));
        });

        SplashScreen splash = new SplashScreen();
        StartupTask task = new StartupTask();
        splash.bind(task);

        task.setOnSucceeded(e -> {
            facade = task.getValue();
            try {
                showMainWindow(stage);
            } catch (RuntimeException ex) {
                log.error("Cannot open main window", ex);
                Ui.showError("Cannot start application", String.valueOf(ex.getMessage()));
                Platform.exit();
                return;
            }
            splash.close();   // closed only after the main window is visible
        });

        task.setOnFailed(e -> {
            Throwable error = task.getException();
            log.error("Cannot start application", error);
            String text = String.valueOf(error.getMessage());
            if (error.getCause() != null) {
                text += "\n" + error.getCause().getMessage();
            }
            Ui.showError("Cannot start application", text);
            Platform.exit();
        });

        splash.show();
        Thread thread = new Thread(task, "startup");
        thread.setDaemon(true);
        thread.start();
    }

    private void showMainWindow(Stage stage) {
        pages.add(new DashboardPage(facade));
        pages.add(new GadgetsPage(facade));
        pages.add(new CustomersPage(facade));
        pages.add(new TechniciansPage(facade));
        pages.add(new OrdersPage(facade));
        pages.add(new ReportsPage(facade));

        String[] names = {
                "🏠  Dashboard", "📱  Gadgets", "👤  Customers",
                "🔧  Technicians", "🧾  Repair orders", "📊  Reports"
        };

        Label logo = new Label("FixMyGadget");
        logo.getStyleClass().add("logo");
        Label logoSub = new Label("Repair service CRM");
        logoSub.getStyleClass().add("logo-sub");
        VBox brand = new VBox(2, logo, logoSub);
        brand.setPadding(new Insets(0, 0, 26, 8));

        VBox sidebar = new VBox(6);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(250);
        sidebar.getChildren().add(brand);

        for (int i = 0; i < pages.size(); i++) {
            Button button = new Button(names[i]);
            button.getStyleClass().add("nav-button");
            button.setMaxWidth(Double.MAX_VALUE);
            int index = i;
            button.setOnAction(e -> openPage(index));
            navButtons.add(button);
            sidebar.getChildren().add(button);
        }


        Button closeButton = new Button("✕");
        closeButton.getStyleClass().add("close-button");
        closeButton.setOnAction(e -> {
            log.info("FixMyGadget closed");
            Platform.exit();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox topBar = new HBox(spacer, closeButton);
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER_RIGHT);

        content.setTop(topBar);
        root.setLeft(sidebar);
        root.setCenter(content);
        root.getStyleClass().add("window-root");

        makeDraggable(stage, topBar);
        makeDraggable(stage, sidebar);


        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("FixMyGadget");

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        scene.getStylesheets().add(Ui.CSS);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();

        openPage(0);
    }

    private void makeDraggable(Stage stage, Node node) {
        node.setOnMousePressed(e -> {
            dragX = e.getSceneX();
            dragY = e.getSceneY();
        });
        node.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - dragX);
            stage.setY(e.getScreenY() - dragY);
        });
    }

    private void openPage(int index) {
        for (int i = 0; i < navButtons.size(); i++) {
            Button button = navButtons.get(i);
            button.getStyleClass().remove("active");
            if (i == index) {
                button.getStyleClass().add("active");
            }
        }
        Page page = pages.get(index);
        content.setCenter(page);
        Ui.run(page::refresh);
    }
}