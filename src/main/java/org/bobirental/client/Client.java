package org.bobirental.client;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.bobirental.common.model.Person;

import java.time.LocalDate;

@Entity
@Table(name = "client")
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "client_id")),
        @AttributeOverride(name = "name", column = @Column(name = "client_name")),
        @AttributeOverride(name = "surname", column = @Column(name = "client_surname")),
})
public class Client extends Person {
    @Column(name = "client_address")
    @NotBlank
    @Size(max = 50, message = "{validation.name.size.too_long}")
    private String clientAddress;

    @Column(name = "client_mail")
    @NotBlank
    @Size(max = 40, message = "{validation.name.size.too_long}")
    private String clientMail;

    @Column(name = "client_has_duty")
    @NotNull
    private boolean clientHasDuty;

    @Column(name = "client_removal_date")
    private LocalDate clientRemovalDate;

    public Client() {
        this.clientHasDuty = false;
        this.clientRemovalDate = LocalDate.now().plusYears(1);
    }

    public String getClientAddress() {
        return this.clientAddress;
    }
    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    public String getClientMail() {
        return this.clientMail;
    }

    public void setClientMail(String clientMail) {
        this.clientMail = clientMail;
    }

    public boolean hasClientDuty() {
        return this.clientHasDuty;
    }

    public void setClientHasDuty(boolean clientHasDuty) {
        this.clientHasDuty = clientHasDuty;
    }

    public LocalDate getClientRemovalDate() {
        return this.clientRemovalDate;
    }

    public void setClientRemovalDate(LocalDate clientRemovalDate) {
        this.clientRemovalDate = clientRemovalDate;
    }
}
