package com.fixmygadget.dao;

import com.fixmygadget.model.Gadget;
import com.fixmygadget.model.GadgetType;

import java.util.List;
/**
 * Defines data access operations specific to gadget entities.
 *
 * <p>Extends {@link GenericDao} with queries for finding gadgets
 * by manufacturer, gadget type, partial name combined with type,
 * and screen dimensions within specified ranges.</p>
 *
 * <p>Design pattern: Data Access Object (DAO).</p>
 *
 * <p>Encapsulates gadget-specific retrieval requirements behind
 * a dedicated interface, keeping database query details separate
 * from application and business logic.</p>
 */
public interface GadgetDao extends GenericDao<Gadget, Integer> {
    List<Gadget> findByManufacturer(String manufacturer);

    List<Gadget> findByType(GadgetType type);

    List<Gadget> findByNameAndType(String name, GadgetType type);

    List<Gadget> findByScreenSize(int x1, int y1, int x2, int y2);
}