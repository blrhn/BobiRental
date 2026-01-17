package org.bobirental.client;

import org.bobirental.client.dto.ClientRequest;
import org.bobirental.common.impl.BaseService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ClientService extends BaseService<Client> {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        super(clientRepository);
        this.clientRepository = clientRepository;
    }

    public Integer saveClient(ClientRequest clientRequest) {
        Client client = new Client();

        client.setName(clientRequest.name());
        client.setSurname(clientRequest.surname());
        client.setClientAddress(clientRequest.clientAddress());
        client.setClientMail(clientRequest.clientMail());

        return clientRepository.save(client).getId();
    }

    public boolean canClientCreateAgreement(Integer clientId) {
        return clientRepository.canClientCreateAgreement(clientId);
    }

    public BigDecimal getDebt(Integer clientId) {
        return clientRepository.getDebtById(clientId);
    }

    public BigDecimal getFees(Integer clientId) {
        return clientRepository.getFeesById(clientId);
    }
}
