package org.bobirental.rental.fee;

import jakarta.persistence.EntityNotFoundException;
import org.bobirental.client.Client;
import org.bobirental.client.ClientRepository;
import org.bobirental.employee.Employee;
import org.bobirental.employee.EmployeeRepository;
import org.bobirental.employee.EmployeeRole;
import org.bobirental.rental.agreement.RentalAgreement;
import org.bobirental.rental.agreement.RentalAgreementRepository;
import org.bobirental.rental.fee.dto.FeeRequest;
import org.bobirental.tool.AvailabilityStatus;
import org.bobirental.tool.Tool;
import org.bobirental.tool.ToolCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FeeServiceTest {
    @Mock
    private FeeRepository feeRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private RentalAgreementRepository rentalAgreementRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private FeeService feeService;

    private FeeRequest feeRequest;
    private Fee mockFee;
    private Client mockClient;
    private Tool mockTool;
    private Employee mockEmployee;
    private RentalAgreement mockRentalAgreement;

    @BeforeEach
    public void setUp() {
        feeRequest = new FeeRequest(
                FeeCategory.RENTAL_FEE,
                1,
                1,
                1,
                new BigDecimal("250.45"),
                LocalDate.now().plusDays(7),
                null,
                false
        );

        setMockClient();
        setMockTool();
        setMockEmployee();
        setMockRentalAgreement();
        setMockFee();

    }

    private void setMockClient() {
        mockClient = new Client();
        mockClient.setId(1);
        mockClient.setName("Jan");
        mockClient.setSurname("Kowalski");
        mockClient.setClientAddress("ul. Adresowa 5, 60-664 Poznań");
        mockClient.setClientMail("j.kowalski@mail.com");
    }

    private void setMockFee() {
        mockFee = new Fee();
        mockFee.setId(1);
        mockFee.setIsFeePaid(false);
        mockFee.setFeeCategory(FeeCategory.RENTAL_FEE);
        mockFee.setClient(mockClient);
        mockFee.setActualFee(new BigDecimal("250.45"));
        mockFee.setEmployee(mockEmployee);
        mockFee.setAgreement(mockRentalAgreement);
        mockFee.setFeeDutyDate(LocalDate.now().plusDays(7));
    }

    private void setMockTool() {
        mockTool = new Tool();
        mockTool.setId(1);
        mockTool.setToolAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        mockTool.setToolName("Wiertarka Frania");
        mockTool.setToolDescription("Wiertarka do drobnych śrubek");
        mockTool.setToolCategory(ToolCategory.DRILL);
        mockTool.setToolPrice(new BigDecimal("650.50"));
        mockTool.setToolEntryDate(LocalDate.now());
    }

    private void setMockRentalAgreement() {
        mockRentalAgreement = new RentalAgreement();
        mockRentalAgreement.setEmployee(mockEmployee);
        mockRentalAgreement.setTool(mockTool);
        mockRentalAgreement.setId(1);
        mockRentalAgreement.setAgreementEstimatedTerminationDate(LocalDate.now().plusDays(7));
        mockRentalAgreement.setClient( mockClient);
        mockRentalAgreement.setAgreementComment("Na potrzeby remontowe kuchni");
    }

    private void setMockEmployee() {
        mockEmployee = new Employee();
        mockEmployee.setId(1);
        mockEmployee.setName("Mariola");
        mockEmployee.setSurname("Tomaszewska");
        mockEmployee.setEmployeeRole(EmployeeRole.WAREHOUSE_MANAGER);
        mockEmployee.setEmployeeLogin("m.tomaszewska");
        mockEmployee.setEmployeePassword("haslo123");
    }

    // Test tworzenia nowej oplaty z odpowiednimi danymi
    @Test
    public void testCreateFee_ShouldCreateFee_WithValidData() {
        // given odpowiednie encje
        when(rentalAgreementRepository.findById(1)).thenReturn(Optional.of(mockRentalAgreement));
        when(clientRepository.findById(1)).thenReturn(Optional.of(mockClient));
        when(employeeRepository.findById(1)).thenReturn(Optional.of(mockEmployee));
        when(feeRepository.save(any(Fee.class))).thenReturn(mockFee);

        // when wywolanie metody createFee
        Integer feeId =  feeService.createFee(feeRequest);

        // then sprawdzenie czy id jest rowne odpowiedniej wartosci
        assertNotNull(feeId);
        assertEquals(1, feeId);

        // weryfikacja wywolania metody save z odpowiednimi paramterami
        verify(feeRepository).save(argThat(fee ->
            fee.getActualFee().equals(new BigDecimal("250.45")) &&
            fee.getFeeCategory().equals(FeeCategory.RENTAL_FEE) &&
            fee.getClient().getId().equals(1) &&
            fee.getAgreement().getId().equals(1)
        ));
    }

    // Test tworzenia nowej oplaty z nieistniejaca umowa
    @Test
    public void testCreateFee_ShouldThrowException_WhenInvalidAgreement() {
        // given
        FeeRequest tempFeeRequest = new FeeRequest(
                FeeCategory.RENTAL_FEE,
                999,
                1,
                1,
                new BigDecimal("250.45"),
                LocalDate.now().plusDays(7),
                null,
                false);
        when(rentalAgreementRepository.findById(999)).thenReturn(Optional.empty());

        // when proba utworzenia nowej oplaty oraz then sprawdzenie wiadomosci zwiazanej z wyrzuconym
        // wyjatkiem oraz weryfikacja braku wywolania metody save
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> feeService.createFee(tempFeeRequest));

        assertEquals("Rental Agreement Not Found", exception.getMessage());
        verify(feeRepository, never()).save(any(Fee.class));
    }

    // Test aktualizowania oplaty - oplacenia jej
    @Test
    public void testUpdateFee_ShouldSetIsFeePaidTrue() {
        // given
        FeeRequest tempFeeRequest = new FeeRequest(
                FeeCategory.RENTAL_FEE,
                1,
                1,
                1,
                new BigDecimal("250.45"),
                LocalDate.now().plusDays(7),
                LocalDate.now(),
                false);
        when(feeRepository.findById(1)).thenReturn(Optional.of(mockFee));
        when(feeRepository.save(any(Fee.class))).thenReturn(mockFee);

        // when
        feeService.updateFee(tempFeeRequest, 1);

        // then weryfikacja czy metoda save jest wywolywana z odpowiednio ustawiona flaga i wartoscia pola
        verify(feeRepository).save(argThat(fee -> fee.isFeePaid() && fee.getFeeFinalizedDate() != null));
    }

    // Test pobierania wszystkich nieoplaconych oplat klienta
    @Test
    public void testFindUnpaidFeesByClientId_ShouldReturnListOfFees() {
        // given
        when(feeRepository.findUnpaidFeesByClientId(1)).thenReturn(Collections.singletonList(mockFee));

        // when
        List<Fee> unpaidFees =  feeService.findUnpaidFeesByClientId(1);

        // then sprawdzenie czy lista ma odpowiednia dlugosc oraz czy flaga informujaca o zaplacie
        // nie jest postawiona
        assertNotNull(unpaidFees);
        assertEquals(1, unpaidFees.size());
        assertFalse(unpaidFees.getFirst().isFeePaid());

        // weryfikacja wywolania metody
        verify(feeRepository).findUnpaidFeesByClientId(1);
    }

    // Test pobierania wszystkich zaleglych oplat klienta
    @Test
    public void testFindOverdueFeesByClientId_ShouldReturnListOfFees() {
        // given
        when(feeRepository.findOverdueFeesByClientId(1)).thenReturn(Collections.singletonList(mockFee));

        // when
        List<Fee> overDueFees = feeService.findOverdueFeesByClientId(1);

        // then
        assertNotNull(overDueFees);
        assertEquals(1, overDueFees.size());
        assertFalse(overDueFees.getFirst().isFeePaid());
        LocalDate finalizationDate = overDueFees.getFirst().getFeeFinalizedDate();
        assertTrue(finalizationDate == null || finalizationDate.isBefore(LocalDate.now()));

        verify(feeRepository).findOverdueFeesByClientId(1);
    }
}
