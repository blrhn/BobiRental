package org.bobirental.rental.fee;

import org.bobirental.common.impl.BaseService;
import org.springframework.stereotype.Service;

@Service
public class FeeService extends BaseService<Fee> {
    public FeeService(FeeRepository feeRepository) {
        super(feeRepository);
    }
}
