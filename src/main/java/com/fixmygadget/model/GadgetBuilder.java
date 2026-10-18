package com.fixmygadget.model;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class GadgetBuilder implements Builder<Gadget> {
    private Integer id;
    private String name;
    private String manufacturer;
    private BigDecimal price;
    private GadgetType type;
    private int screenWidth;
    private int screenHeight;
    private final Set<Feature> features = new HashSet<>();

    public GadgetBuilder id(Integer id) {
        this.id = id;
        return this;
    }

    public GadgetBuilder name(String name) {
        this.name = name;
        return this;
    }

    public GadgetBuilder manufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
        return this;
    }

    public GadgetBuilder price(BigDecimal price) {
        this.price = price;
        return this;
    }

    public GadgetBuilder type(GadgetType type) {
        this.type = type;
        return this;
    }

    public GadgetBuilder screen(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        return this;
    }

    public GadgetBuilder feature(Feature feature) {
        this.features.add(feature);
        return this;
    }

    @Override
    public Gadget build() {
        if (name == null || manufacturer == null || price == null || type == null) {
            throw new IllegalStateException("name, manufacturer, price and type are required");
        }
        Gadget gadget = new Gadget(name, manufacturer, price, type, screenWidth, screenHeight);
        gadget.setId(id);
        for (Feature f : features) {
            gadget.addFeature(f);
        }
        return gadget;
    }
}