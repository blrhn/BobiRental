package org.bobirental.rental.agreement;

import org.bobirental.common.impl.BaseController;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rental_agreements")
public class RentalAgreementController extends BaseController<RentalAgreement> {
    private final RentalAgreementService rentalAgreementService;

    public RentalAgreementController(RentalAgreementService rentalAgreementService) {
        super(rentalAgreementService);
        this.rentalAgreementService = rentalAgreementService;
    }

    @PostMapping("/create")
    public Integer createRentalAgreement(@RequestBody RentalAgreement rentalAgreement) {
        return rentalAgreementService.createRentalAgreement(rentalAgreement);
    }

    @PostMapping("/{agreementId}/close")
    public void closeAgreement(@PathVariable Integer agreementId, @RequestParam Integer employeeId) {
        rentalAgreementService.closeAgreement(agreementId, employeeId);
    }
}
