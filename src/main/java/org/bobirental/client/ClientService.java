package org.bobirental.client;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClientService {
    private final ClientRepository clients;

    public ClientService(ClientRepository clients) {
        this.clients = clients;
    }

    public List<Client> findAllClients() {
        return clients.findAll();
    }

    public Client findClientById(Integer id) {
         Optional<Client> optionalClient = clients.findById(id);

        return optionalClient.orElseThrow(
                () -> new IllegalArgumentException("Client with id " + id + " does not exist"));

    }

    public Client saveClient(Client client) {
        return clients.save(client);
    }

    public Client updateClient(Client client) {
        if (clients.existsById(client.getId())) {
            return clients.save(client);
        }

        throw new IllegalArgumentException("Client with id " + client.getId() + " does not exist");
    }

    public void deleteClientById(Integer id) {
        if (clients.existsById(id)) {
            clients.deleteById(id);
        }

        throw new IllegalArgumentException("Client with id " + id + " does not exist");
    }
}
