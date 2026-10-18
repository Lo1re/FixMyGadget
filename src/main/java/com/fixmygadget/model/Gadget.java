package com.fixmygadget.model;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

//class designed for a tesk

public class Gadget {
    private Integer id;
    private String name;
    private String manufacturer;
    private BigDecimal price;
    private GadgetType type;
    private int screenWidth;
    private int screenHeight;
    private Set<Feature> features = new HashSet<>();

    //empty constrctor
    public Gadget() {

    }

    //full constructor
    public Gadget(String name, String manufacturer, BigDecimal price, GadgetType type, int screenWidth, int screenHeight) {
        this.name = name;
        this.manufacturer = manufacturer;
        this.price = price;
        this.type = type;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    //copy
    public Gadget(Gadget other) {
        this.id = other.id;
        this.name = other.name;
        this.manufacturer = other.manufacturer;
        this.price = other.price;
        this.type = other.type;
        this.screenWidth = other.screenWidth;
        this.screenHeight = other.screenHeight;
        this.features = new HashSet<>();
        this.features.addAll(other.features);
    }

    public void addFeature(Feature feature) {
        features.add(feature);
    }

    public void removeFeature(Feature feature) {
        features.remove(feature);
    }

    public boolean hasFeature(Feature feature) {
        return features.contains(feature);
    }

    // analog of >>
    public void writeTo(Writer writer) throws IOException {
        String featuresText = "";
        for (Feature f : features) {
            if (!featuresText.isEmpty()) {
                featuresText += ",";
            }
            featuresText += f.name();
        }
        writer.write(name + ";" + manufacturer + ";" + price + ";" + type.name() + ";" + screenWidth + ";" + screenHeight + ";" + featuresText + "\n");
        writer.flush();
    }

    // analog of <<
    public boolean readFrom(Reader reader) throws IOException {
        String line = readLine(reader);
        if (line == null) {
            return false;
        }
        String[] parts = line.split(";", -1);
        this.name = parts[0];
        this.manufacturer = parts[1];
        this.price = new BigDecimal(parts[2]);
        this.type = GadgetType.valueOf(parts[3]);
        this.screenWidth = Integer.parseInt(parts[4]);
        this.screenHeight = Integer.parseInt(parts[5]);
        this.features = new HashSet<>();
        if (!parts[6].isEmpty()) {
            for (String f : parts[6].split(",")) {
                this.features.add(Feature.valueOf(f));
            }
        }
        return true;
    }

    private String readLine(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        int ch = reader.read();
        if (ch == -1) {
            return null;
        }
        while (ch != -1 && ch != '\n') {
            if (ch != '\r') {
                sb.append((char) ch);
            }
            ch = reader.read();
        }
        return sb.toString();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public GadgetType getType() {
        return type;
    }

    public void setType(GadgetType type) {
        this.type = type;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
    }

    public Set<Feature> getFeatures() {
        return features;
    }

    @Override
    public String toString() {
        return name + " (" + manufacturer + ", " + type + ", " + price + ")";
    }
}