package org.bobirental.client;

import org.bobirental.common.impl.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ClientRepository extends BaseRepository<Client> {
    @Query(value = "SELECT calculate_client_debt(:clientId)")
    BigDecimal getDebtById(@Param("clientId") Integer clientId);

    @Query(value = "SELECT total_client_fees(:clientId)")
    BigDecimal getFeesById(@Param("clientId") Integer clientId);

    @Query(value = "SELECT can_client_create_agreement(:clientId)")
    boolean canClientCreateAgreement(@Param("clientId") Integer clientId);

    List<Client> findByClientRemovalDateAndClientHasDutyFalse(LocalDate clientRemovalDate);
}
