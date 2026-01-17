package org.bobirental.rental.fee;

import org.bobirental.common.impl.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Repository
public interface FeeRepository extends BaseRepository<Fee> {
    @Query(value = "SELECT f FROM Fee f WHERE f.client.id = :clientId AND f.isFeePaid = FALSE")
    List<Fee> findUnpaidFeesByClientId(@Param("clientId") Integer clientId);

    @Query(value = "SELECT f FROM Fee f WHERE f.client.id = :clientId AND f.feeDutyDate < CURRENT_DATE")
    List<Fee> findOverdueFeesByClientId(@Param("clientId") Integer clientId);

    @Query(value = "SELECT f FROM Fee f WHERE f.agreement.id = :agreementId")
    List<Fee> findFeesByAgreementId(@Param("agreementId") Integer agreementId);

    @Query(value = "SELECT DISTINCT f.client.id FROM Fee f WHERE f.feeDutyDate < :now AND f.isFeePaid = FALSE")
    Set<Integer> findClientIdsWithOverdueFees(@Param("now") LocalDate now);
}
