package com.fixmygadget.model;

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
