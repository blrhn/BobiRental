package org.bobirental.rental.fee;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.bobirental.common.impl.BaseController;
import org.bobirental.rental.fee.dto.FeeRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fees")
@Tag(name = "Fees")
public class FeeController extends BaseController<Fee> {

    private final FeeService feeService;

    public FeeController(FeeService feeService) {
        super(feeService);
        this.feeService = feeService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('REGULAR_EMPLOYEE', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "Add fee")
    public Integer createFee(@RequestBody FeeRequest feeRequest) {
        return feeService.createFee(feeRequest);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('REGULAR_EMPLOYEE', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "Update fee")
    public Integer updateFee(@RequestBody FeeRequest feeRequest, @PathVariable Integer id) {
        return feeService.updateFee(feeRequest, id);
    }

    @GetMapping("/unpaid/{id}")
    @PreAuthorize("hasAnyRole('REGULAR_EMPLOYEE', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "Get unpaid fees by client id")
    public List<Fee> getUnpaidFees(@PathVariable Integer id) {
        return feeService.findUnpaidFeesByClientId(id);
    }

    @GetMapping("/overdue/{id}")
    @PreAuthorize("hasAnyRole('REGULAR_EMPLOYEE', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "Get overdue fees by client id")
    public List<Fee> getOverdueFees(@PathVariable Integer id) {
        return feeService.findOverdueFeesByClientId(id);
    }

    @GetMapping("/agreement/{id}")
    @PreAuthorize("hasAnyRole('REGULAR_EMPLOYEE', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "Get fees by agreement id")
    public List<Fee> getAgreementFees(@PathVariable Integer id) {
        return feeService.findFeesByAgreementId(id);
    }
}
