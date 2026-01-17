package org.bobirental.rental.agreement.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RentalAgreementRequest(
        @NotNull
        LocalDate agreementEstimatedTerminationDate,

        @NotNull
        Integer clientId,

        @NotNull
        Integer toolId,

        @NotNull
        Integer employeeId,

        @Size(max = 300)
        String agreementComment) {}