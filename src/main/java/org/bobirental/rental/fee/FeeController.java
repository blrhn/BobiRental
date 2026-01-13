package org.bobirental.rental.fee;

import org.bobirental.common.impl.BaseController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/fees")
public class FeeController extends BaseController<Fee> {

    private final FeeService feeService;

    public FeeController(FeeService feeService) {
        super(feeService);
        this.feeService = feeService;
    }

    @GetMapping("{clientId}/unpaid")
    List<Fee> getUnpaidFees(@PathVariable Integer clientId) {
        return feeService.findUnpaidFeesByClientId(clientId);
    }

    @GetMapping("{clientId}/overdue")
    List<Fee> getOverdueFees(@PathVariable Integer clientId) {
        return feeService.findOverdueFeesByClientId(clientId);
    }

    @GetMapping("{agreementId}/agreement")
    List<Fee> getAgreementFees(@PathVariable Integer agreementId) {
        return feeService.findFeesByAgreementId(agreementId);
    }
}
