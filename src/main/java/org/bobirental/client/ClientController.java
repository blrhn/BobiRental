package org.bobirental.client;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public List<Client> getAllClients() {
        return clientService.findAllClients();
    }

    @GetMapping("/{clientId}")
    public Client getClient(@PathVariable Integer clientId) {
        return clientService.findClientById(clientId);
    }

    @PostMapping
    public Client createClient(@RequestBody Client client) {
        return clientService.saveClient(client);
    }

    @PutMapping("/{clientId}")
    public Client updateClient(@RequestBody Client client,  @PathVariable Integer clientId) {
        return clientService.updateClient(client);
    }

    @DeleteMapping("/{clientId}")
    public void deleteClient(@PathVariable Integer clientId) {
        clientService.deleteClientById(clientId);
    }

}
