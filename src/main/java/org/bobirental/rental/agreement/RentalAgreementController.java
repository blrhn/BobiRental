package org.bobirental.rental.agreement;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.bobirental.common.impl.BaseController;
import org.bobirental.rental.agreement.dto.RentalAgreementRequest;
import org.springframework.http.ResponseEntity;
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
    public Integer createRentalAgreement(@RequestBody RentalAgreementRequest rentalAgreement) {
        return rentalAgreementService.createRentalAgreement(rentalAgreement);
    }

    @Operation(summary = "Close rental agreement by id")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    @PostMapping("/close/{id}/")
    public ResponseEntity<Void> closeAgreement(@PathVariable Integer id, @RequestParam Integer employeeId) {
        rentalAgreementService.closeAgreement(id, employeeId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get rental agreement by client id")
    @PreAuthorize("hasAnyRole('REGULAR_EMPLOYEE', 'WAREHOUSE_MANAGER')")
    @GetMapping("/client/{id}")
    public List<RentalAgreement> findRentalAgreementByClientId(@PathVariable Integer id) {
        return rentalAgreementService.findRentalAgreementByClientId(id);
    }

    @Operation(summary = "Get rental agreement by tool id")
    @PreAuthorize("hasAnyRole('REGULAR_EMPLOYEE', 'WAREHOUSE_MANAGER')")
    @GetMapping("/tool/{id}")
    public List<RentalAgreement> findRentalAgreementByToolId(@PathVariable Integer id) {
        return rentalAgreementService.findRentalAgreementByToolId(id);
    }

    @Operation(summary = "Get all overdue agreements")
    @PreAuthorize("hasAnyRole('REGULAR_EMPLOYEE', 'WAREHOUSE_MANAGER')")
    @GetMapping("/overdue")
    public List<RentalAgreement> findOverdueRentalAgreements() {
        return rentalAgreementService.findOverdueRentalAgreements();
    }

    @Operation(summary = "Initializes a return: when all ids match (scenariusz)")
    @PostMapping("/initiate-return/{id}")
    @PreAuthorize("hasAnyRole('REGULAR_EMPLOYEE', 'WAREHOUSE_MANAGER')")
    public void initiateReturn(@PathVariable Integer id, @RequestParam Integer clientId) {
        rentalAgreementService.initiateReturn(id, clientId);
    }

    @Operation(summary = "Toggles has_penalty flag")
    @PutMapping("/toggle-has-penalty/{id}")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    public boolean toggleHasPenalty(@PathVariable Integer id) {
        return rentalAgreementService.toggleHasPenalty(id);
    }

    @Operation(summary = "Get all rental agreements that have to be reviewed")
    @GetMapping("/to-review")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    public List<RentalAgreement> findAllRentalAgreementsToBeReviewed() {
        return rentalAgreementService.findAllRentalAgreementsToBeReviewed();
    }

    @Operation(summary = "Get rental agreement that has to be reviewed by its id")
    @GetMapping("/to-review/{id}")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    public RentalAgreement findRentalAgreementToBeReviewed(@PathVariable Integer id) {
        return rentalAgreementService.findRentalAgreementToBeReviewed(id);
    }
}
