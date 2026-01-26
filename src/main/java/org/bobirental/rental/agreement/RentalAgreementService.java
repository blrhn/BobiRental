package org.bobirental.rental.agreement;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.bobirental.client.Client;
import org.bobirental.client.ClientRepository;
import org.bobirental.common.impl.BaseService;
import org.bobirental.employee.Employee;
import org.bobirental.employee.EmployeeRepository;
import org.bobirental.rental.agreement.dto.RentalAgreementRequest;
import org.bobirental.tool.Tool;
import org.bobirental.tool.ToolRepository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RentalAgreementService extends BaseService<RentalAgreement> {
    private final RentalAgreementRepository rentalAgreementRepository;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final ToolRepository toolRepository;

    public RentalAgreementService(
            RentalAgreementRepository rentalAgreementRepository,
            ClientRepository clientRepository,
            EmployeeRepository employeeRepository,
            ToolRepository toolRepository,
            JdbcTemplate jdbcTemplate) {
        super(rentalAgreementRepository);
        this.rentalAgreementRepository = rentalAgreementRepository;
        this.clientRepository = clientRepository;
        this.employeeRepository = employeeRepository;
        this.toolRepository = toolRepository;
    }

    public Integer createRentalAgreement(RentalAgreementRequest rentalAgreement) {
        Client client = clientRepository
                .findById(rentalAgreement.clientId()).orElseThrow(() -> new EntityNotFoundException("Client not found"));
        Employee employee = employeeRepository
                .findById(rentalAgreement.employeeId()).orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        Tool tool = toolRepository
                .findById(rentalAgreement.toolId()).orElseThrow(() -> new EntityNotFoundException("Tool not found"));

        RentalAgreement agreement = new RentalAgreement();
        agreement.setClient(client);
        agreement.setEmployee(employee);
        agreement.setTool(tool);
        agreement.setAgreementEstimatedTerminationDate(rentalAgreement.agreementEstimatedTerminationDate());
        agreement.setAgreementComment(rentalAgreement.agreementComment());

        if (agreement.getAgreementComment() == null) {
            return rentalAgreementRepository.createRentalAgreement(
                    client.getId(),
                    tool.getId(),
                    employee.getId(),
                    agreement.getAgreementEstimatedTerminationDate());
        } else {
            return rentalAgreementRepository.createRentalAgreement(
                    client.getId(),
                    tool.getId(),
                    employee.getId(),
                    agreement.getAgreementEstimatedTerminationDate(),
                    agreement.getAgreementComment());
        }
    }

    public void initiateReturn(Integer rentalAgreementId, Integer clientId) {
        RentalAgreement agreement = rentalAgreementRepository
                .findById(rentalAgreementId).orElseThrow(() -> new EntityNotFoundException("Rental Agreement not found"));

        if (agreement.getClient().getId().equals(clientId)) {
            agreement.setToBeReviewed(true);
        }

        rentalAgreementRepository.save(agreement);
    }

    @Transactional
    public void closeAgreement(Integer agreementId, Integer employeeId) {
        rentalAgreementRepository.closeAgreement(agreementId, employeeId);
        /*String sql = "CALL close_agreement(?, ?)";
        jdbcTemplate.update(sql, agreementId, employeeId);*/
    }

    public List<RentalAgreement> findRentalAgreementByClientId(Integer clientId) {
        return rentalAgreementRepository.findRentalAgreementByClientId(clientId);
    }

    public List<RentalAgreement> findRentalAgreementByToolId(Integer toolId) {
        return rentalAgreementRepository.findRentalAgreementByToolId(toolId);
    }

    public List<RentalAgreement> findOverdueRentalAgreements() {
        return rentalAgreementRepository.findOverdueRentalAgreements();
    }

    public List<RentalAgreement> findAllRentalAgreementsToBeReviewed() {
        return rentalAgreementRepository.findRentalAgreementsByToBeReviewedTrueAndIsAgreementTerminatedFalse();
    }

    public RentalAgreement findRentalAgreementToBeReviewed(Integer id) {
        return rentalAgreementRepository.findRentalAgreementByIdAndToBeReviewedTrueAndIsAgreementTerminatedFalse(id);
    }

    public boolean toggleHasPenalty(Integer id) {
        RentalAgreement rentalAgreement = rentalAgreementRepository
                .findById(id).orElseThrow(() -> new EntityNotFoundException("Rental Agreement not found"));

        boolean hasPenalty = rentalAgreement.getHasPenalty();
        rentalAgreement.setHasPenalty(!hasPenalty);

        rentalAgreementRepository.save(rentalAgreement);

        return !hasPenalty;

    }

}
