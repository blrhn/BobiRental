package org.bobirental.client;

import org.bobirental.common.impl.BaseService;
import org.springframework.stereotype.Service;

@Service
public class ClientService extends BaseService<Client> {
    public ClientService(ClientRepository clientRepository) {
        super(clientRepository);
    }
}
