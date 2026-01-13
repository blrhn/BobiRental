package org.bobirental.rental.agreement;

import org.bobirental.common.impl.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

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

    @Query(value = "SELECT r FROM RentalAgreement r WHERE r.client.id = :clientId")
    List<RentalAgreement> findRentalAgreementByClientId(@Param("clientId") Integer clientId);

    @Query(value = "SELECT r FROM RentalAgreement r WHERE r.tool.id = :toolId")
    List<RentalAgreement> findRentalAgreementByToolId(@Param("toolId") Integer toolId);

    @Query(value = "SELECT ra FROM RentalAgreement ra WHERE ra.isAgreementTerminated = false AND ra.agreementEstimatedTerminationDate < CURRENT_DATE")
    List<RentalAgreement> findOverdueRentalAgreements();
}
