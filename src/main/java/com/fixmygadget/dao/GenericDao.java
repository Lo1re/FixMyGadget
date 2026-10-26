package com.fixmygadget.dao;

import java.util.List;
/**
 * Defines the generic contract for data access operations on entities.
 *
 * <p>Provides common CRUD operations: saving, updating, deleting,
 * retrieving an entity by its identifier, and retrieving all entities.
 * Generic type parameters allow the same contract to be reused for
 * different entity types and identifier types.</p>
 *
 * <p>Design patterns: Data Access Object (DAO) and Generic Repository
 * abstraction.</p>
 *
 * <p>Separates data access contracts from their implementations,
 * allowing business logic to work with entity operations without
 * depending on database-specific code.</p>
 *
 * @param <T> the entity type managed by the DAO
 * @param <ID> the type of the entity identifier
 */
public interface GenericDao<T, ID> {
    void save(T entity);
    void update(T entity);
    void delete(ID id);
    T findById(ID id);
    List<T> findAll();
}