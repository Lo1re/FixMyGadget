package com.fixmygadget.dao;

import com.fixmygadget.model.Gadget;
import com.fixmygadget.model.GadgetType;

import java.util.List;

public interface GadgetDao extends GenericDao<Gadget, Integer> {
    List<Gadget> findByManufacturer(String manufacturer);

    List<Gadget> findByType(GadgetType type);

    List<Gadget> findByNameAndType(String name, GadgetType type);

    List<Gadget> findByScreenSize(int x1, int y1, int x2, int y2);
}