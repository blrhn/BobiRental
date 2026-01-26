package org.bobirental.rental.fee;

import jakarta.persistence.EntityNotFoundException;
import org.bobirental.client.Client;
import org.bobirental.client.ClientRepository;
import org.bobirental.common.impl.BaseService;
import org.bobirental.employee.Employee;
import org.bobirental.employee.EmployeeRepository;
import org.bobirental.rental.agreement.RentalAgreement;
import org.bobirental.rental.agreement.RentalAgreementRepository;
import org.bobirental.rental.fee.dto.FeeRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeeService extends BaseService<Fee> {

    private final FeeRepository feeRepository;
    private final RentalAgreementRepository rentalAgreementRepository;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;

    public FeeService(
            FeeRepository feeRepository,
            RentalAgreementRepository rentalAgreementRepository,
            ClientRepository clientRepository,
            EmployeeRepository employeeRepository) {
        super(feeRepository);
        this.feeRepository = feeRepository;
        this.rentalAgreementRepository = rentalAgreementRepository;
        this.clientRepository = clientRepository;
        this.employeeRepository = employeeRepository;
    }

    public List<Fee> findUnpaidFeesByClientId(Integer clientId) {
        return feeRepository.findUnpaidFeesByClientId(clientId);
    }

    public List<Fee> findOverdueFeesByClientId(Integer clientId) {
        return feeRepository.findOverdueFeesByClientId(clientId);
    }

    public List<Fee> findFeesByAgreementId(Integer agreementId) {
        return feeRepository.findFeesByAgreementId(agreementId);
    }

    public Integer createFee(FeeRequest feeRequest) {
        RentalAgreement agreement = rentalAgreementRepository
                .findById(feeRequest.rentalAgreementId()).orElseThrow(() -> new EntityNotFoundException("Rental Agreement Not Found"));
        Client client = clientRepository
                .findById(feeRequest.clientId()).orElseThrow(() -> new EntityNotFoundException("Client Not Found"));
        Employee employee = employeeRepository
                .findById(feeRequest.employeeId()).orElseThrow(() -> new EntityNotFoundException("Employee Not Found"));

        Fee fee = new Fee();

        fee.setClient(client);
        fee.setAgreement(agreement);
        fee.setEmployee(employee);
        fee.setActualFee(feeRequest.actualFee());
        fee.setFeeCategory(feeRequest.feeCategory());
        fee.setFeeDutyDate(feeRequest.feeDutyDate());

        return feeRepository.save(fee).getId();
    }

    public Integer updateFee(FeeRequest feeRequest, Integer id) {
        Fee existingFee = this.findEntityById(id);

        existingFee.setActualFee(feeRequest.actualFee());
        existingFee.setFeeCategory(feeRequest.feeCategory());
        existingFee.setFeeDutyDate(feeRequest.feeDutyDate());
        existingFee.setFeeFinalizedDate(feeRequest.feeFinalizedDate());
        existingFee.setIsFeePaid(feeRequest.isFeePaid());

        if (existingFee.getFeeFinalizedDate() != null) {
            existingFee.setIsFeePaid(true);
        }

        return feeRepository.save(existingFee).getId();
    }
}
