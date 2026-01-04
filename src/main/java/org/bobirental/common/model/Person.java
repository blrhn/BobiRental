package org.bobirental.common.model;

import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public class Person extends BaseEntity {
    private String name;
    private String surname;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return this.surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }
}
