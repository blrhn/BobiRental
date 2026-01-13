package org.bobirental.client;

import org.bobirental.rental.agreement.RentalAgreementRepository;
import org.bobirental.rental.fee.FeeRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ClientLifecycleScheduler {
    private final ClientRepository clientRepository;
    private final RentalAgreementRepository rentalAgreementRepository;
    private final FeeRepository feeRepository;

    public ClientLifecycleScheduler(
            ClientRepository clientRepository,
            RentalAgreementRepository rentalAgreementRepository,
            FeeRepository feeRepository) {
        this.clientRepository = clientRepository;
        this.rentalAgreementRepository = rentalAgreementRepository;
        this.feeRepository = feeRepository;
    }

    @Scheduled(cron = "0 0 6,19 * * *")
    public void deleteOldClients() {
        LocalDate now = LocalDate.now();

        List<Client> oldClients = clientRepository.findByClientRemovalDateAndClientHasDutyFalse(now);
        Set<Integer> oldClientsIds = oldClients
                .stream()
                .map(Client::getId)
                .collect(Collectors.toSet());

        Set<Integer> clientIdsWithActiveAgreement = rentalAgreementRepository.findClientIdsWithActiveAgreements(oldClientsIds);

        List<Client> clientsToDelete = oldClients
                .stream()
                .filter(c -> !clientIdsWithActiveAgreement.contains(c.getId()))
                .toList();

        if (!clientsToDelete.isEmpty()) {
            clientRepository.deleteAll(clientsToDelete);
        }
    }

    @Scheduled(cron = "0 0 6,19 * * *")
    public void updateClientHasDuty() {
        LocalDate now = LocalDate.now();
        Set<Integer> clientIdsWithOverdueFees = feeRepository.findClientIdsWithOverdueFees(now);
        List<Client> allClients = clientRepository.findAll();
        List<Client> clientsToUpdate = new ArrayList<>();

        for  (Client client : allClients) {
            boolean hasDebt = clientIdsWithOverdueFees.contains(client.getId());

            if (client.hasClientDuty() != hasDebt) {
                client.setClientHasDuty(true);
                clientsToUpdate.add(client);
            }
        }

        if (!clientsToUpdate.isEmpty()) {
            clientRepository.saveAll(clientsToUpdate);
        }
    }
}
