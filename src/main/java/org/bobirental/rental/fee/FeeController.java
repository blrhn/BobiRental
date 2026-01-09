package org.bobirental.rental.fee;

import org.bobirental.common.impl.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fees")
public class FeeController extends BaseController<Fee> {

    public FeeController(FeeService feeService) {
        super(feeService);
    }
}
