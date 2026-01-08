package org.bobirental.client;

import org.bobirental.common.impl.BaseController;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clients")
public class ClientController extends BaseController<Client> {
    public ClientController(ClientService clientService) {
        super(clientService);
    }
}
