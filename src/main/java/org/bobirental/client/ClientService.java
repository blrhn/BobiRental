package org.bobirental.client;

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

    public BigDecimal getDebt(Integer clientId) {
        return clientRepository.getDebtById(clientId);
    }

    public BigDecimal getFees(Integer clientId) {
        return clientRepository.getFeesById(clientId);
    }
}
