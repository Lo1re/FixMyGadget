package com.fixmygadget.model;
/**
 * Represents a technician employed by the FixMyGadget service center.
 *
 * <p>Stores the technician's identifier, first name, and last name.
 * Provides constructors, accessor methods, and a human-readable
 * string representation.</p>
 *
 * <p>Role: Domain model. Technician instances can be associated
 * with repair orders to identify the person responsible for
 * carrying out a repair.</p>
 */
public class Technician {
    private Integer id;
    private String lastName;
    private String firstName;

    public Technician() {
    }

    public Technician(String lastName, String firstName) {
        this.lastName = lastName;
        this.firstName = firstName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;

    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Override
    public String toString() {
        return lastName + " " + firstName;
    }


}
