package org.bobirental.rental.agreement;

import org.bobirental.common.impl.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface RentalAgreementRepository extends BaseRepository<RentalAgreement> {
    @Query(value = "SELECT create_rental_agreement_func(:clientId, :toolId, :employeeId, :estimatedDate, :comment)")
    Integer createRentalAgreement(Integer clientId, Integer toolId, Integer employeeId, LocalDate estimatedDate, String comment);
    @Query(value = "SELECT create_rental_agreement_func(:clientId, :toolId, :employeeId, :estimatedDate, :comment)")
    Integer createRentalAgreement(Integer clientId, Integer toolId, Integer employeeId, LocalDate estimatedDate);

    @Query(value = "CALL close_agreement(:agreementId, :employeeId)", nativeQuery = true)
    void closeAgreement(Integer agreementId, Integer employeeId);
}
