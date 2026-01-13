package org.bobirental.client;

import org.bobirental.common.impl.BaseController;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/clients")
public class ClientController extends BaseController<Client> {
    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        super(clientService);
        this.clientService = clientService;
    }

    @GetMapping("{id}/debt")
    public BigDecimal getDebt(@PathVariable Integer id) {
        return clientService.getDebt(id);
    }

    @GetMapping("{id}/fees")
    public BigDecimal getFees(@PathVariable Integer id) {
        return clientService.getFees(id);
    }
}
