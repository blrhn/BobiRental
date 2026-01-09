package org.bobirental.rental.agreement;

import org.bobirental.common.impl.BaseService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class RentalAgreementService extends BaseService<RentalAgreement> {
    private final RentalAgreementRepository rentalAgreementRepository;

    public RentalAgreementService(RentalAgreementRepository rentalAgreementRepository) {
        super(rentalAgreementRepository);
        this.rentalAgreementRepository = rentalAgreementRepository;
    }

    public Integer createRentalAgreement(RentalAgreement rentalAgreement) {
        Integer clientId = rentalAgreement.getClient().getId();
        Integer toolId = rentalAgreement.getTool().getId();
        Integer employeeId = rentalAgreement.getEmployee().getId();
        LocalDate estimatedDate = rentalAgreement.getAgreementEstimatedTerminationDate();
        String comment = rentalAgreement.getAgreementComment();

        if (comment == null) {
            return rentalAgreementRepository.createRentalAgreement(clientId, toolId, employeeId, estimatedDate);
        } else {
            return rentalAgreementRepository.createRentalAgreement(clientId, toolId, employeeId, estimatedDate, comment);
        }
    }

    public void closeAgreement(Integer agreementId, Integer employeeId) {
        rentalAgreementRepository.closeAgreement(agreementId, employeeId);
    }
}
