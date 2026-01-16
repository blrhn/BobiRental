package org.bobirental.rental.agreement;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.bobirental.common.impl.BaseController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rental_agreements")
@Tag(name = "Rental Agreements")
public class RentalAgreementController extends BaseController<RentalAgreement> {
    private final RentalAgreementService rentalAgreementService;

    public RentalAgreementController(RentalAgreementService rentalAgreementService) {
        super(rentalAgreementService);
        this.rentalAgreementService = rentalAgreementService;
    }

    @Operation(summary = "Create rental agreement")
    @PostMapping("/create")
    public Integer createRentalAgreement(@RequestBody RentalAgreement rentalAgreement) {
        return rentalAgreementService.createRentalAgreement(rentalAgreement);
    }

    @Operation(summary = "Close rental agreement by id")
    @PostMapping("/close/{id}/")
    public void closeAgreement(@PathVariable Integer id, @RequestParam Integer employeeId) {
        rentalAgreementService.closeAgreement(id, employeeId);
    }

    @Operation(summary = "Get rental agreement by client id")
    @GetMapping("/client/{id}")
    List<RentalAgreement> findRentalAgreementByClientId(@PathVariable Integer id) {
        return rentalAgreementService.findRentalAgreementByClientId(id);
    }

    @Operation(summary = "Get rental agreement by tool id")
    @GetMapping("/tool/{id}")
    List<RentalAgreement> findRentalAgreementByToolId(@PathVariable Integer id) {
        return rentalAgreementService.findRentalAgreementByToolId(id);
    }

    @Operation(summary = "Get all overdue agreements")
    @GetMapping("/overdue")
    List<RentalAgreement> findOverdueRentalAgreements() {
        return rentalAgreementService.findOverdueRentalAgreements();
    }

}
