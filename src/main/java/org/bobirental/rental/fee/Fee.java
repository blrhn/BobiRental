package org.bobirental.rental.fee;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.bobirental.client.Client;
import org.bobirental.common.model.BaseEntity;
import org.bobirental.employee.Employee;
import org.bobirental.rental.agreement.RentalAgreement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
@Table(name = "fee")
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "fee_id")),
})
public class Fee extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(name = "fee_category")
    @NotNull
    private FeeCategory feeCategory;

    @ManyToOne
    @JoinColumn(name = "agreement_id")
    @NotNull
    private RentalAgreement agreement;

    @ManyToOne
    @JoinColumn(name = "client_id")
    @NotNull
    private Client client;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    @NotNull
    private Employee employee;

    @Column(name = "actual_fee")
    @NotNull
    private BigDecimal actualFee;

    @Column(name = "is_fee_paid")
    @NotNull
    private boolean isFeePaid;

    @Column(name = "fee_duty_date")
    @NotNull
    private LocalDate feeDutyDate;

    @Column(name = "fee_finalized_date")
    private LocalDate feeFinalizedDate;

    public Fee() {
        this.isFeePaid = false;
    }

    public FeeCategory getFeeCategory() {
        return this.feeCategory;
    }

    public void setFeeCategory(FeeCategory feeCategory) {
        this.feeCategory = feeCategory;
    }

    public RentalAgreement getAgreement() {
        return this.agreement;
    }

    public void setAgreement(RentalAgreement agreement) {
        this.agreement = agreement;
    }

    public Client getClient() {
        return this.client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Employee getEmployee() {
        return this.employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public BigDecimal getActualFee() {
        return this.actualFee.setScale(2, RoundingMode.HALF_UP);
    }

    public void setActualFee(BigDecimal actualFee) {
        this.actualFee = actualFee.setScale(2, RoundingMode.HALF_UP);
    }

    public boolean isFeePaid() {
        return this.isFeePaid;
    }
}
