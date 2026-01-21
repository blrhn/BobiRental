package org.bobirental;

import jakarta.transaction.Transactional;
import org.bobirental.client.Client;
import org.bobirental.client.ClientRepository;
import org.bobirental.client.ClientService;
import org.bobirental.employee.Employee;
import org.bobirental.employee.EmployeeRepository;
import org.bobirental.employee.EmployeeRole;
import org.bobirental.employee.EmployeeService;
import org.bobirental.rental.agreement.RentalAgreement;
import org.bobirental.rental.agreement.RentalAgreementRepository;
import org.bobirental.rental.agreement.RentalAgreementService;
import org.bobirental.rental.agreement.dto.RentalAgreementRequest;
import org.bobirental.tool.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class RentingToolIntegrationTests {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ToolRepository toolRepository;

    @Autowired
    private RentalAgreementRepository  rentalAgreementRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private RentalAgreementService  rentalAgreementService;

    @Autowired
    private ToolService toolService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private EmployeeService employeeService;

    private Client testClient;
    private Employee testEmployee;
    private Tool testTool;

    @BeforeEach
    public void setUp() {
        setTestClient();
        setTestEmployee();
        setTestTool();
    }

    private void setTestClient() {
        testClient = new Client();
        testClient.setName("Jan");
        testClient.setSurname("Kowalski");
        testClient.setClientAddress("ul. Budowlana 5, 50-338 Wrocław");
        testClient.setClientMail("jan.kowalski@mail.com");
        testClient.setClientHasDuty(false);

        testClient = clientRepository.save(testClient);
    }

    private void setTestEmployee() {
        testEmployee = new Employee();
        testEmployee.setName("Justyna");
        testEmployee.setSurname("Kowalczyk");
        testEmployee.setEmployeeLogin("j.kowalczyk");
        testEmployee.setEmployeePassword("haslo123");
        testEmployee.setEmployeeRole(EmployeeRole.REGULAR_EMPLOYEE);

        testEmployee = employeeRepository.save(testEmployee);
    }

    private void setTestTool() {
        testTool = new Tool();
        testTool.setToolName("Wiertarka Frania");
        testTool.setToolPrice(new BigDecimal("650.45"));
        testTool.setToolCategory(ToolCategory.DRILL);
        testTool.setToolDescription("Wiertarka do niezwykle małych śrubek");
        testTool.setToolAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        testTool.setToolEntryDate(LocalDate.now());

        testTool = toolRepository.save(testTool);
    }

    // Test zgodny z diagramem: Wypozyczenie sprzetu Budowlanego: pracownik zna numer sprzetu
    @Test
    void testCompleteRentingToolProcess_ShouldCreateAgreementSuccessfully_WhenEmployeeKnowsToolId() {
        // pracownik zna numer sprzetu - wprowadzenie nr sprzetu
        Integer toolId = testTool.getId();
        // pozostałe wartosci znane przez pracownika
        Integer clientId =  testClient.getId();
        Integer employeeId = testEmployee.getId();

        // Weryfikacja statusu klienta i dostepnosci sprzetu

        // Wartosc oczekiwana: pobranie klienta z bazy danych
        // oraz tym samym: brak wyswietlenia wiadomosci: "Klient nie powinien byc null"
        Client clientFromDb = clientService.findEntityById(clientId);
        assertNotNull(clientFromDb, "Klient nie powinien byc null");
        // Uzyskano: Brak wyswietlenia wiadomosci: "Klient nie powinien byc null"

        // Wartosc oczekiwana: flaga true oznaczajaca, ze klient moze zawrzec nowa umowe
        // oraz tym samym: brak wyswietlenia wiadomosci: "Klient powinien moc utworzyc umowe"
        boolean canCreateAgreement = clientService.canClientCreateAgreement(clientId);
        assertTrue(canCreateAgreement, "Klient powinien moc utworzyc umowe");
        // Uzyskano: Brak wyswietlenia wiadomosci: "Klient powinien moc utworzyc umowe"

        // Wartosc oczekiwana: dostepny sprzet z bazy danych
        // oraz tym samym: brak wyswietlenia wiadomosci: "Sprzet nie powinien byc null" oraz "Sprzet powinien byc dostepny"
        Tool toolFromDb = toolService.findEntityById(toolId);
        assertNotNull(toolFromDb, "Sprzet nie powinien byc null");
        assertEquals(AvailabilityStatus.AVAILABLE, toolFromDb.getToolAvailabilityStatus(), "Sprzet powinien byc dostepny");

        // Zatwierdzenie wypozyczenia oraz przygotowanie danych do umowy
        // Rejestracja wypozyczenia sprzetu, wylaczenie sprzetu z dostepnosci
        // Wprowadzenie danych klienta i okresu wypozyczenia

        LocalDate estimatedEndDate = LocalDate.now().plusDays(7);
        RentalAgreementRequest agreementRequest = new RentalAgreementRequest(
                estimatedEndDate,
                clientId,
                toolId,
                employeeId,
                "Na potrzeby remontu kuchni"
        );

        // Powyzsze + generowanie numeru umowy + rejestracja umowy w bazie danych
        // Wartosc oczekiwana: Numer nowoutworzonej umowy oraz tym samym brak wiadomosci: "System powinien wygenerowac nr umowy"
        Integer agreementId =  rentalAgreementService.createRentalAgreement(agreementRequest);
        // potwierdzenie zawarcia umowy
        assertNotNull(agreementId, "System powinien wygenerowac nr umowy");
        // Uzyskano: Brak wyswietlenia wiadomosci: "System powinien wygenerowac nr umowy"

        // Sprawdzenie potwierdzenia zawarcia umowy

        // Wartosc oczekiwana: Nowoutworzona ummowa oraz tym samym brak wyrzucenia wyjatku:
        // "Umowa nie została zapisana w bazie"
        RentalAgreement createdAgreement = rentalAgreementRepository
                .findById(agreementId)
                .orElseThrow(() -> new AssertionError("Umowa nie została zapisana w bazie"));

        // Sprawdzenie wszystkich danych umowy
        // Oczekiwany wynik: Brak wyswietlenia zadnego z ponizszych komunikatow
        assertEquals(clientId, createdAgreement.getClient().getId(),
                "Nr klienta w umowie musi byc poprawny");
        assertEquals(toolId, createdAgreement.getTool().getId(),
                "Nr sprzętu w umowie musi byc poprawny");
        assertEquals(employeeId, createdAgreement.getEmployee().getId(),
                "Nr pracownika w umowie musi byc poprawny");
        assertEquals(LocalDate.now(), createdAgreement.getAgreementExecutionDate(),
                "Data rozpoczecia to dzisiejsza data");
        assertEquals(estimatedEndDate, createdAgreement.getAgreementEstimatedTerminationDate(),
                "Data szacowanego zakokonzenia musi być poprawna");
        assertFalse(createdAgreement.isAgreementTerminated(),
                "Nowa umowa nie powinna być zamknieta");
        assertFalse(createdAgreement.isToBeReviewed(),
                "Nowa umowa nie powinna być do przeglądu");
        // Uzyskano: Brak wyswietlenia zadnego z ponizszych komunikatow
    }
}
