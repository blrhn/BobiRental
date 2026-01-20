package org.bobirental.rental.agreement;

import jakarta.persistence.EntityNotFoundException;
import org.bobirental.client.Client;
import org.bobirental.client.ClientRepository;
import org.bobirental.employee.Employee;
import org.bobirental.employee.EmployeeRepository;
import org.bobirental.employee.EmployeeRole;
import org.bobirental.rental.agreement.dto.RentalAgreementRequest;
import org.bobirental.tool.AvailabilityStatus;
import org.bobirental.tool.Tool;
import org.bobirental.tool.ToolCategory;
import org.bobirental.tool.ToolRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RentalAgreementServiceTest {
    @Mock
    private RentalAgreementRepository rentalAgreementRepository;

    @Mock
    ClientRepository clientRepository;

    @Mock
    EmployeeRepository employeeRepository;

    @Mock
    ToolRepository toolRepository;

    @InjectMocks
    private RentalAgreementService rentalAgreementService;

    private RentalAgreementRequest rentalAgreementRequest;
    private RentalAgreement mockRentalAgreement;
    private Client mockClient;
    private Employee mockEmployee;
    private Tool mockTool;

    @BeforeEach
    public void setUp() {
        rentalAgreementRequest = new RentalAgreementRequest(
                LocalDate.now().plusDays(7),
                1,
                1,
                1,
                "Na potrzeby remontowe kuchni"
        );

        setMockClient();
        setMockEmployee();
        setMockTool();
        setMockRentalAgreement();
    }

    private void setMockClient() {
        mockClient = new Client();
        mockClient.setId(1);
        mockClient.setName("Jan");
        mockClient.setSurname("Kowalski");
        mockClient.setClientAddress("ul. Adresowa 5, 60-664 Poznań");
        mockClient.setClientMail("j.kowalski@mail.com");
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

    // Test tworzenia umowy z komentarzem
    @Test
    public void testCreateRentalAgreement_ShouldCallMethodWithCommentParameter() {
        // given odpowiednie encje
        when(clientRepository.findById(1)).thenReturn(Optional.of(mockClient));
        when(toolRepository.findById(1)).thenReturn(Optional.of(mockTool));
        when(employeeRepository.findById(1)).thenReturn(Optional.of(mockEmployee));
        when(rentalAgreementRepository
                .createRentalAgreement(eq(1), eq(1), eq(1), any(LocalDate.class), anyString()))
                .thenReturn(1);

        // when wywolanie metody createRentalAgreement
        Integer id = rentalAgreementService.createRentalAgreement(rentalAgreementRequest);

        // then sprawdzenie poprawnosci zwroconego id oraz weryfikacja wywolania (lub nie) odpowiednich metod
        assertNotNull(id);
        assertEquals(1, id);
        verify(rentalAgreementRepository)
                .createRentalAgreement(eq(1), eq(1), eq(1), any(LocalDate.class), anyString());
        verify(rentalAgreementRepository, never())
                .createRentalAgreement(anyInt(), anyInt(), anyInt(), any(LocalDate.class));

    }

    // Test tworzenia umowy bez komentarza
    @Test
    public void testCreateRentalAgreement_ShouldCallMethodWithoutCommentParameter() {
        // given odpowiednie encje
        RentalAgreementRequest tempRequest = new RentalAgreementRequest(
                LocalDate.now().plusDays(7), 1, 1, 1, null);

        when(clientRepository.findById(1)).thenReturn(Optional.of(mockClient));
        when(toolRepository.findById(1)).thenReturn(Optional.of(mockTool));
        when(employeeRepository.findById(1)).thenReturn(Optional.of(mockEmployee));
        when(rentalAgreementRepository
                .createRentalAgreement(eq(1), eq(1), eq(1), any(LocalDate.class)))
                .thenReturn(1);

        // when wywolanie metody createRentalAgreement
        Integer id = rentalAgreementService.createRentalAgreement(tempRequest);

        // then sprawdzenie poprawnosci zwroconego id oraz weryfikacja wywolania (lub nie) odpowiednich metod
        assertNotNull(id);
        assertEquals(1, id);
        verify(rentalAgreementRepository)
                .createRentalAgreement(eq(1), eq(1), eq(1), any(LocalDate.class));
        verify(rentalAgreementRepository, never())
                .createRentalAgreement(anyInt(), anyInt(), anyInt(), any(LocalDate.class), anyString());

    }

    // Test tworzenia umowy z nieistniejacym klientem
    @Test
    public void testCreateRentalAgreement_ShouldThrowException_WhenClientNotFound() {
        // given brak klienta
        when(clientRepository.findById(1)).thenReturn(Optional.empty());

        // when (wywolanie metody createRentalAgreement) and then sprawdzenie czy metoda wyrzucila wyjatek
        // oraz weryfikacja, ze metoda createRentalAgreement nigdy nie zostala wywolana
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> rentalAgreementService.createRentalAgreement(rentalAgreementRequest));

        assertEquals("Client not found", exception.getMessage());

        verify(rentalAgreementRepository, never())
                .createRentalAgreement(anyInt(), anyInt(), anyInt(), any(LocalDate.class), anyString());

    }

    // Test tworzenia umowy z nieistniejacym narzedziem
    @Test
    public void testCreateRentalAgreement_ShouldThrowException_WhenToolNotFound() {
        // given odpowiednie encje oraz brak narzedzia
        when(clientRepository.findById(1)).thenReturn(Optional.of(mockClient));
        when(employeeRepository.findById(1)).thenReturn(Optional.of(mockEmployee));
        when(toolRepository.findById(1)).thenReturn(Optional.empty());

        // when (wywolanie metody createRentalAgreement) and then sprawdzenie czy metoda wyrzucila wyjatek
        // oraz weryfikacja, ze metoda createRentalAgreement nigdy nie zostala wywolana
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> rentalAgreementService.createRentalAgreement(rentalAgreementRequest));

        assertEquals("Tool not found", exception.getMessage());

        verify(rentalAgreementRepository, never())
                .createRentalAgreement(anyInt(), anyInt(), anyInt(), any(LocalDate.class), anyString());
    }

    // Test rozpoczęcia procedury zwrotu sprzętu - sprawdzenie poprawnosci danych oraz ustawienie flagi,
    // ze umowa jest gotowa do rozpatrzenia
    @Test
    public void testInitiateReturn_ShouldSetToBeReviewed_WhenAllIdsMatch() {
        // given
        when(rentalAgreementRepository.findById(1)).thenReturn(Optional.of(mockRentalAgreement));
        when(rentalAgreementRepository.save(any(RentalAgreement.class))).thenReturn(mockRentalAgreement);

        // when wywolanie metody initiateReturn
        rentalAgreementService.initiateReturn(1, 1);

        // then weryfikacja czy umowa jest zapisywana z poprawnie ustawiona flaga
        verify(rentalAgreementRepository).save(argThat(RentalAgreement::isToBeReviewed));
    }

    // Test rozpoczęcia procedury zwrotu sprzętu - sprawdzenie, ze flaga pozostaje false
    //  w przypadku niezgodnosci id
    @Test
    public void testInitiateReturn_ShouldNotSetToBeReviewed_WhenIdsMismatch() {
        // given
        when(rentalAgreementRepository.findById(1)).thenReturn(Optional.of(mockRentalAgreement));
        when(rentalAgreementRepository.save(any(RentalAgreement.class))).thenReturn(mockRentalAgreement);

        // when wywolanie metody initiateReturn
        rentalAgreementService.initiateReturn(1, 999);

        // then weryfikacja czy umowa jest zapisywana z poprawnie ustawiona flaga
        verify(rentalAgreementRepository).save(argThat(agreement -> !agreement.isToBeReviewed()));
    }

    // Test wywolania procedury zamykajacej umowe
    @Test
    public void testCloseAgreement() {
        // given
        doNothing().when(rentalAgreementRepository).closeAgreement(1, 1);

        // when
        rentalAgreementService.closeAgreement(1, 1);

        // then
        verify(rentalAgreementRepository).closeAgreement(1, 1);
    }

    // Test pobierania umow zwiazanych z klientem
    @Test
    public void testFindRentalAgreementsByClientId_ShouldReturnAgreementsList() {
        // given
        when(rentalAgreementRepository
                .findRentalAgreementByClientId(1)).thenReturn(Collections.singletonList(mockRentalAgreement));

        // when
        List<RentalAgreement> agreements = rentalAgreementService.findRentalAgreementByClientId(1);

        // then
        assertNotNull(agreements);
        assertEquals(1, agreements.size());
        assertEquals(1, agreements.getFirst().getId());

        verify(rentalAgreementRepository).findRentalAgreementByClientId(1);
    }

    // Test pobierania zadluzonych umow zwiazanych z klientem
    @Test
    public void testFindOverdueRentalAgreements_ShouldReturnAgreementsList() {
        // given odpowiednia encja zadluzonej umowy o 3 dni
        RentalAgreement overdueAgreement = new RentalAgreement();
        overdueAgreement.setId(2);
        overdueAgreement.setAgreementEstimatedTerminationDate(LocalDate.now().minusDays(3));
        when(rentalAgreementRepository
                .findOverdueRentalAgreements()).thenReturn(Collections.singletonList(overdueAgreement));

        // when wywolanie metody findOverdueRentalAgreements()
        List<RentalAgreement> overdueAgreements =  rentalAgreementService.findOverdueRentalAgreements();

        // then sprawdzenie czy lista zawiera 1 element oraz czy zwrocona umowa zawiera date starsza niz dzis
        assertNotNull(overdueAgreements);
        assertEquals(1, overdueAgreements.size());
        assertTrue(overdueAgreements.getFirst().getAgreementEstimatedTerminationDate().isBefore(LocalDate.now()));

        // weryfikacja wywolania odpowiedniej metody
        verify(rentalAgreementRepository).findOverdueRentalAgreements();

    }

    // Test pobierania umow, ktore powinien przejrzec szef magazynu
    @Test
    public void testFindAllRentalAgreementsToBeReviewed_ShouldReturnAUnreviewedAgreementsList() {
        // given
        mockRentalAgreement.setToBeReviewed(true);
        when(rentalAgreementRepository
                .findRentalAgreementsByToBeReviewedTrueAndIsAgreementTerminatedFalse())
                .thenReturn(Collections.singletonList(mockRentalAgreement));

        // when
        List<RentalAgreement> agreementsToBeReviewed = rentalAgreementService.findAllRentalAgreementsToBeReviewed();

        // then sprawdzenie czy lista zawiera jeden element i sprawdzenie poprawnosci flag umowy
        assertNotNull(agreementsToBeReviewed);
        assertEquals(1, agreementsToBeReviewed.size());
        assertTrue(agreementsToBeReviewed.getFirst().isToBeReviewed());
        assertFalse(agreementsToBeReviewed.getFirst().isAgreementTerminated());

        // weryfikacja wywolania metody
        verify(rentalAgreementRepository).findRentalAgreementsByToBeReviewedTrueAndIsAgreementTerminatedFalse();
    }
}
