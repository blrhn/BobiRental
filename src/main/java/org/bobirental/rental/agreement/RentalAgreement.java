package org.bobirental.rental.agreement;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.bobirental.client.Client;
import org.bobirental.common.model.BaseEntity;
import org.bobirental.employee.Employee;
import org.bobirental.tool.Tool;

import java.time.LocalDate;

@Entity
@Table(name = "rental_agreement")
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "agreement_id")),
})
public class RentalAgreement extends BaseEntity {
    @Column(name = "agreement_execution_date")
    @NotNull
    private LocalDate agreementExecutionDate;

    @Column(name = "agreement_estimated_termination_date")
    @NotNull
    private LocalDate agreementEstimatedTerminationDate;

    @Column(name = "agreement_actual_termination_date")
    private LocalDate agreementActualTerminationDate;

    @Column(name = "is_agreement_terminated")
    @NotNull
    private boolean isAgreementTerminated;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "tool_id")
    private Tool tool;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "agreement_comment")
    @Size(max = 300, message = "{validation.name.size.too_long}")
    private String agreementComment;

    public RentalAgreement() {
        this.agreementExecutionDate = LocalDate.now();
        this.isAgreementTerminated = false;
    }

    public LocalDate getAgreementExecutionDate() {
        return this.agreementExecutionDate;
    }

    public void setAgreementExecutionDate(LocalDate agreementExecutionDate) {
        this.agreementExecutionDate = agreementExecutionDate;
    }

    public LocalDate getAgreementEstimatedTerminationDate() {
        return this.agreementEstimatedTerminationDate;
    }

    public void setAgreementEstimatedTerminationDate(LocalDate agreementEstimatedTerminationDate) {
        this.agreementEstimatedTerminationDate = agreementEstimatedTerminationDate;
    }

    public LocalDate getAgreementActualTerminationDate() {
        return this.agreementActualTerminationDate;
    }

    public void setAgreementActualTerminationDate(LocalDate agreementActualTerminationDate) {
        this.agreementActualTerminationDate = agreementActualTerminationDate;
    }

    public boolean isAgreementTerminated() {
        return this.isAgreementTerminated;
    }

    public void setAgreementTerminated(boolean agreementTerminated) {
        this.isAgreementTerminated = agreementTerminated;
    }

    public Client getClient() {
        return this.client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Tool getTool() {
        return this.tool;
    }

    public void setTool(Tool tool) {
        this.tool = tool;
    }

    public Employee getEmployee() {
        return this.employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}
