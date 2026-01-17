package org.bobirental.client;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.bobirental.common.impl.BaseController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/clients")
@Tag(name = "Clients")
public class ClientController extends BaseController<Client> {
    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        super(clientService);
        this.clientService = clientService;
    }

    @Operation(summary = "Get client debt by client id")
    @PreAuthorize("hasAnyRole('REGULAR_EMPLOYEE', 'WAREHOUSE_MANAGER')")
    @GetMapping("/debt/{id}")
    public BigDecimal getDebt(@PathVariable Integer id) {
        return clientService.getDebt(id);
    }

    @Operation(summary = "Get client fees by client id")
    @PreAuthorize("hasAnyRole('REGULAR_EMPLOYEE', 'WAREHOUSE_MANAGER')")
    @GetMapping("/fees/{id}")
    public BigDecimal getFees(@PathVariable Integer id) {
        return clientService.getFees(id);
    }
}
