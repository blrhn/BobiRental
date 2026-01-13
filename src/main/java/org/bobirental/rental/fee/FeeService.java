package org.bobirental.rental.fee;

import org.bobirental.common.impl.BaseService;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeeService extends BaseService<Fee> {

    private final FeeRepository feeRepository;

    public FeeService(FeeRepository feeRepository) {
        super(feeRepository);
        this.feeRepository = feeRepository;
    }

    public List<Fee> findUnpaidFeesByClientId(Integer clientId) {
        return feeRepository.findUnpaidFeesByClientId(clientId);
    }

    public List<Fee> findOverdueFeesByClientId(Integer clientId) {
        return feeRepository.findOverdueFeesByClientId(clientId);
    }

    public List<Fee> findFeesByAgreementId(Integer agreementId) {
        return feeRepository.findFeesByAgreementId(agreementId);
    }

}
