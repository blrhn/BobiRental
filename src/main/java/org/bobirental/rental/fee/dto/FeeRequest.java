package org.bobirental.rental.fee.dto;

import jakarta.validation.constraints.NotNull;
import org.bobirental.rental.fee.FeeCategory;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FeeRequest(
        @NotNull
        FeeCategory feeCategory,

        @NotNull
        Integer rentalAgreementId,

        @NotNull
        Integer clientId,

        @NotNull
        Integer employeeId,

        @NotNull
        BigDecimal actualFee,

        @NotNull
        LocalDate feeDutyDate,

        LocalDate feeFinalizedDate
) { }
