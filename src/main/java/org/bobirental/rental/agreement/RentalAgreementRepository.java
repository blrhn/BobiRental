package org.bobirental.rental.agreement;

import org.bobirental.common.impl.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface RentalAgreementRepository extends BaseRepository<RentalAgreement> {
    @Query(value = "SELECT create_rental_agreement_func(:clientId, :toolId, :employeeId, :estimatedDate, :comment)")
    Integer createRentalAgreement(
            @Param("clientId") Integer clientId,
            @Param("toolId") Integer toolId,
            @Param("employeeId") Integer employeeId,
            @Param("estimatedDate") LocalDate estimatedDate,
            @Param("comment") String comment);

    @Query(value = "SELECT create_rental_agreement_func(:clientId, :toolId, :employeeId, :estimatedDate, :comment)")
    Integer createRentalAgreement(
            @Param("clientId") Integer clientId,
            @Param("toolId") Integer toolId,
            @Param("employeeId") Integer employeeId,
            @Param("estimatedDate") LocalDate estimatedDate);

    @Query(value = "CALL close_agreement(:agreementId, :employeeId)", nativeQuery = true)
    void closeAgreement(@Param("agreementId") Integer agreementId, @Param("employeeId") Integer employeeId);
}
