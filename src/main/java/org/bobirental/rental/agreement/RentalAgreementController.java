package org.bobirental.rental.agreement;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.bobirental.common.impl.BaseController;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAnyRole('REGULAR_EMPLOYEE', 'WAREHOUSE_MANAGER')")
    @PostMapping("/create")
    public Integer createRentalAgreement(@RequestBody RentalAgreement rentalAgreement) {
        return rentalAgreementService.createRentalAgreement(rentalAgreement);
    }

    @Operation(summary = "Close rental agreement by id")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    @PostMapping("/close/{id}/")
    public void closeAgreement(@PathVariable Integer id, @RequestParam Integer employeeId) {
        rentalAgreementService.closeAgreement(id, employeeId);
    }

    @Operation(summary = "Get rental agreement by client id")
    @PreAuthorize("hasAnyRole('REGULAR_EMPLOYEE', 'WAREHOUSE_MANAGER')")
    @GetMapping("/client/{id}")
    List<RentalAgreement> findRentalAgreementByClientId(@PathVariable Integer id) {
        return rentalAgreementService.findRentalAgreementByClientId(id);
    }

    @Operation(summary = "Get rental agreement by tool id")
    @PreAuthorize("hasAnyRole('REGULAR_EMPLOYEE', 'WAREHOUSE_MANAGER')")
    @GetMapping("/tool/{id}")
    List<RentalAgreement> findRentalAgreementByToolId(@PathVariable Integer id) {
        return rentalAgreementService.findRentalAgreementByToolId(id);
    }

    @Operation(summary = "Get all overdue agreements")
    @PreAuthorize("hasAnyRole('REGULAR_EMPLOYEE', 'WAREHOUSE_MANAGER')")
    @GetMapping("/overdue")
    List<RentalAgreement> findOverdueRentalAgreements() {
        return rentalAgreementService.findOverdueRentalAgreements();
    }
}
