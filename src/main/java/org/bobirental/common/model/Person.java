package org.bobirental.common.model;

import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotBlank;

@MappedSuperclass
public class Person extends BaseEntity {
    @NotBlank
    private String name;
    @NotBlank
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
