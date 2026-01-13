package org.bobirental.rental.fee;

import org.bobirental.common.impl.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeRepository extends BaseRepository<Fee> {
    @Query(value = "SELECT f FROM Fee f WHERE f.client.id = :clientId AND f.isFeePaid = FALSE")
    List<Fee> findUnpaidFeesByClientId(@Param("clientId") Integer clientId);

    @Query(value = "SELECT f FROM Fee f WHERE f.client.id = :clientId AND f.feeDutyDate < CURRENT_DATE")
    List<Fee> findOverdueFeesByClientId(@Param("clientId") Integer clientId);

    @Query(value = "SELECT f FROM Fee f WHERE f.agreement.id = :agreementId")
    List<Fee> findFeesByAgreementId(@Param("agreementId") Integer agreementId);
}
