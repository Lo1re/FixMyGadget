package com.fixmygadget.model;
/**
 * Defines a generic contract for constructing objects of a specified type.
 *
 * <p>Declares the {@code build()} method, which creates and returns
 * an instance of the target type using the configuration provided
 * by a concrete builder.</p>
 *
 * <p>Design pattern: Builder. This interface standardizes the final
 * object creation step and allows different builders to share the
 * same construction contract.</p>
 *
 * @param <T> the type of object produced by the builder
 */
public interface Builder <T>{
    T build();
}
