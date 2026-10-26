package com.fixmygadget.service;

import com.fixmygadget.dao.GadgetDao;
import com.fixmygadget.exception.NotFoundException;
import com.fixmygadget.model.Feature;
import com.fixmygadget.model.Gadget;
import com.fixmygadget.model.GadgetType;
import com.fixmygadget.sort.GadgetSortField;
import com.fixmygadget.sort.QuickSort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
/**
 * Provides business operations for managing gadgets.
 *
 * <p>Coordinates gadget-related operations, validates business requirements,
 * and delegates data persistence to the appropriate DAO.</p>
 *
 * <p>Separates gadget business logic from the user interface and
 * database access layers.</p>
 */
public class GadgetService {
    private static final Logger log = LoggerFactory.getLogger(GadgetService.class);

    private final GadgetDao gadgetDao;

    public GadgetService(GadgetDao gadgetDao) {
        this.gadgetDao = gadgetDao;
    }

    public void addGadget(Gadget gadget) {
        validate(gadget);
        gadgetDao.save(gadget);
        log.info("Gadget added: {}", gadget);
    }

    public void updateGadget(Gadget gadget) {
        if (gadget.getId() == null) {
            throw new IllegalArgumentException("Gadget has no id");
        }
        validate(gadget);
        getById(gadget.getId());
        gadgetDao.update(gadget);
        log.info("Gadget updated: {}", gadget);
    }

    public void deleteGadget(int id) {
        getById(id);
        gadgetDao.delete(id);
        log.info("Gadget deleted: id={}", id);
    }

    public Gadget getById(int id) {
        Gadget gadget = gadgetDao.findById(id);
        if (gadget == null) {
            throw new NotFoundException("Gadget not found: id=" + id);
        }
        return gadget;
    }

    public List<Gadget> getAll() {
        return gadgetDao.findAll();
    }

    public List<Gadget> findByNameAndType(String name, GadgetType type) {
        return gadgetDao.findByNameAndType(name, type);
    }

    public List<Gadget> findByManufacturer(String manufacturer) {
        return gadgetDao.findByManufacturer(manufacturer);
    }

    public List<Gadget> findByType(GadgetType type) {
        return gadgetDao.findByType(type);
    }

    public List<Gadget> findByScreenSize(int x1, int y1, int x2, int y2) {
        return gadgetDao.findByScreenSize(x1, y1, x2, y2);
    }


    public void addFeature(int gadgetId, Feature feature) {
        Gadget gadget = getById(gadgetId);
        gadget.addFeature(feature);
        gadgetDao.update(gadget);
        log.info("Feature {} added to gadget id={}", feature, gadgetId);
    }

    public void removeFeature(int gadgetId, Feature feature) {
        Gadget gadget = getById(gadgetId);
        gadget.removeFeature(feature);
        gadgetDao.update(gadget);
        log.info("Feature {} removed from gadget id={}", feature, gadgetId);
    }


    public List<Gadget> sort(List<Gadget> gadgets, GadgetSortField field, boolean ascending) {
        QuickSort.sort(gadgets, field.getComparator(ascending));
        return gadgets;
    }


    public void exportCatalog(Writer writer) throws IOException {
        List<Gadget> gadgets = gadgetDao.findAll();
        for (Gadget gadget : gadgets) {
            gadget.writeTo(writer);
        }
        log.info("Catalog exported: {} gadgets", gadgets.size());
    }

    // returns amount of added hadhets
    public int importCatalog(Reader reader) throws IOException {
        int count = 0;
        Gadget gadget = new Gadget();
        while (gadget.readFrom(reader)) {
            addGadget(gadget);
            count++;
            gadget = new Gadget();
        }
        log.info("Catalog imported: {} gadgets", count);
        return count;
    }
    private void validate(Gadget gadget) throws IllegalArgumentException {
        if (gadget.getName() == null || gadget.getName().isBlank()) {
            throw new IllegalArgumentException("Gadget name is required");
        }
        if (gadget.getManufacturer() == null || gadget.getManufacturer().isBlank()) {
            throw new IllegalArgumentException("Manufacturer is required");
        }
        if (gadget.getType() == null) {
            throw new IllegalArgumentException("Gadget type is required");
        }
        if (gadget.getPrice() == null || gadget.getPrice().signum() < 0) {
            throw new IllegalArgumentException("Price must be 0 or more");
        }
        if (gadget.getScreenWidth() <= 0 || gadget.getScreenHeight() <= 0) {
            throw new IllegalArgumentException("Screen size must be greater than 0");
        }
    }
}