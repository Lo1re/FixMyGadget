package com.fixmygadget.ui;

import com.fixmygadget.facade.FixMyGadgetFacade;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public abstract class Page extends VBox {
    protected final FixMyGadgetFacade facade;

    protected Page(FixMyGadgetFacade facade, String title, String subtitle) {
        this.facade = facade;
        setSpacing(18);
        setPadding(new Insets(28));
        getStyleClass().add("page");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("page-title");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("page-subtitle");
        getChildren().add(new VBox(2, titleLabel, subtitleLabel));
    }

    public abstract void refresh();
}