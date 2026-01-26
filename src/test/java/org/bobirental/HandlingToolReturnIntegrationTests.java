package org.bobirental;

import jakarta.transaction.Transactional;
import org.bobirental.client.Client;
import org.bobirental.client.ClientRepository;
import org.bobirental.employee.Employee;
import org.bobirental.employee.EmployeeRepository;
import org.bobirental.employee.EmployeeRole;
import org.bobirental.rental.agreement.RentalAgreement;
import org.bobirental.rental.agreement.RentalAgreementRepository;
import org.bobirental.rental.agreement.RentalAgreementService;
import org.bobirental.rental.agreement.dto.RentalAgreementRequest;
import org.bobirental.rental.fee.FeeCategory;
import org.bobirental.rental.fee.FeeService;
import org.bobirental.rental.fee.dto.FeeRequest;
import org.bobirental.tool.*;
import org.bobirental.tool.dto.ToolEventRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class HandlingToolReturnIntegrationTests {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ToolRepository toolRepository;

    @Autowired
    private RentalAgreementRepository rentalAgreementRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private RentalAgreementService rentalAgreementService;

    @Autowired
    private ToolService toolService;

    @Autowired
    private FeeService feeService;

    private Client testClient;
    private Employee testRegularEmployee;
    private Employee testWarehouseManager;
    private Tool testTool;
    private RentalAgreement testRentalAgreement;

    @BeforeEach
    public void setup() {
        setTestClient();
        setTestRegularEmployee();
        setTestWarehouseManager();
        setTestTool();
        setActiveRentalAgreement();
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

    private void setTestRegularEmployee() {
        testRegularEmployee = new Employee();
        testRegularEmployee.setName("Justyna");
        testRegularEmployee.setSurname("Kowalczyk");
        testRegularEmployee.setEmployeeLogin("j.kowalczyk");
        testRegularEmployee.setEmployeePassword("haslo123");
        testRegularEmployee.setEmployeeRole(EmployeeRole.REGULAR_EMPLOYEE);

        testRegularEmployee = employeeRepository.save(testRegularEmployee);
    }

    private void setTestWarehouseManager() {
        testWarehouseManager = new Employee();
        testWarehouseManager.setName("Remigiusz");
        testWarehouseManager.setSurname("Gruby");
        testWarehouseManager.setEmployeeLogin("r.gruby");
        testWarehouseManager.setEmployeePassword("haslo123");
        testWarehouseManager.setEmployeeRole(EmployeeRole.WAREHOUSE_MANAGER);

        testWarehouseManager = employeeRepository.save(testWarehouseManager);
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

    private void setActiveRentalAgreement() {
        RentalAgreementRequest request = new RentalAgreementRequest(
                LocalDate.now().plusDays(7),
                testClient.getId(),
                testTool.getId(),
                testRegularEmployee.getId(),
                "Na potrzeby remontu kuchni"
        );

        Integer id = rentalAgreementService.createRentalAgreement(request);
        testRentalAgreement = rentalAgreementRepository.findById(id).orElse(null);
    }

    // Test zgodny z diagramem: Przyjmowanie zwrotu sprzetu budowlanego, sprzet sprawny
    @Test
    public void testHandleToolReturn_WithoutDamage() {
        // Numery: klienta i umowy, ktore wprowadza pracownik
        Integer clientId = testClient.getId();
        Integer agreementId = testRentalAgreement.getId();

        // Weryfikacja poprawnosci danych zwrotu + dodanie sprzetu ze sprawdzonej umowy
        // do listy sprzetow do oceny

        rentalAgreementService.initiateReturn(agreementId, clientId);

        // Wartosc oczekiwana: umowa zostala oznaczona jako gotowa do przegladu
        // oraz tym samym: brak wyswietlenia wiadomosci "Umowa powinna być oznaczona do przeglądu"
        RentalAgreement markedAgreement = rentalAgreementRepository.findById(agreementId).orElseThrow();
        assertTrue(markedAgreement.isToBeReviewed(), "Umowa powinna być oznaczona do przeglądu");
        // Uzyskano: Brak wyswietlenia wiadomosci: "Umowa powinna być oznaczona do przeglądu"

        // Szef magazynu przeglada liste umow do sprawdzenia

        // Wartosc oczekiwana: lista umow, ktore powinny zostac sprawdzone oraz tym samym:
        // brak wiadomosci: "Lista umow do sprawdzenia nie powinna byc pusta" oraz
        // "Umowa z naszym id powinna byc na tej liscie" oraz "Wszystkie metody powinny byc oznaczone do sprawdzenia"
        List<RentalAgreement> agreementsToBeReviewed = rentalAgreementService.findAllRentalAgreementsToBeReviewed();
        assertFalse(agreementsToBeReviewed.isEmpty(), "Lista umow do sprawdzenia nie powinna byc pusta");
        assertTrue(agreementsToBeReviewed.stream()
                .anyMatch(agreement -> agreement.getId().equals(agreementId)),
                "Umowa z naszym id powinna byc na tej liscie");
        assertTrue(agreementsToBeReviewed.stream().allMatch(RentalAgreement::isToBeReviewed),
                "Wszystkie metody powinny byc oznaczone do sprawdzenia");
        // Uzyskano: brak powyzszych wiadomosci

        // Sprzet sprawny
        // Zamkniecie umowy przez szefa magazynu:
        // Oznaczenie sprzetu jako dostepny + zatwierdzenie zakonczenia umowy + Aktualizacja statusu umowy na "nieaktywna"
        // + Rejestracja daty i godziny zwrotu sprzętu

        rentalAgreementService.closeAgreement(agreementId, testWarehouseManager.getId());

        // Weryfikacja zamkniecia umowy
        // Wartosc oczekiwana: flaga ze umowa jest zamknieta jest ustawiona oraz odpowiednia data
        // zakonczenia jej oraz tym samym: brak wiadomosci:
        // "Umowa powinna byc zamknieta" oraz "Data zakonczenia powinna byc dzisiejsza"
        RentalAgreement closedAgreement = rentalAgreementRepository.findById(agreementId).orElseThrow();

        assertTrue(closedAgreement.isAgreementTerminated(), "Umowa powinna byc zamknieta");
        assertEquals(LocalDate.now(), closedAgreement.getAgreementActualTerminationDate(),
                "Data zakonczenia powinna byc dzisiejsza");
        // Uzyskano: brak wiadomosci "Umowa powinna byc zamknieta" oraz "Data zakonczenia powinna byc dzisiejsza"


        // Weryfikacja dostepnosci sprzetu
        // Wartosc oczekiwana: status sprzetu powinien byc ustawiony na dostepny oraz tym samym:
        // brak wiadomosci: "Sprzet powinien być ponownie dostepny"
        Tool returnedTool = toolRepository.findById(testTool.getId()).orElseThrow();
        assertEquals(AvailabilityStatus.AVAILABLE, returnedTool.getToolAvailabilityStatus(),
                "Sprzet powinien być ponownie dostepny");
        // Uzyskano: brak wiadomosci: "Sprzet powinien być ponownie dostepny"
    }

    // Test zgodny z diagramem: Przyjmowanie zwrotu sprzetu budowlanego, sprzet uszkodzony
    @Test
    public void testHandleToolReturn_WithDamage() {
        // Numery: klienta i umowy, ktore wprowadza pracownik
        Integer clientId = testClient.getId();
        Integer agreementId = testRentalAgreement.getId();

        // Weryfikacja poprawnosci danych zwrotu + dodanie sprzetu ze sprawdzonej umowy
        // do listy sprzetow do oceny

        rentalAgreementService.initiateReturn(agreementId, clientId);

        // Wartosc oczekiwana: umowa zostala oznaczona jako gotowa do przegladu
        // oraz tym samym: brak wyswietlenia wiadomosci "Umowa powinna być oznaczona do przeglądu"
        RentalAgreement markedAgreement = rentalAgreementRepository.findById(agreementId).orElseThrow();
        assertTrue(markedAgreement.isToBeReviewed(), "Umowa powinna być oznaczona do przeglądu");
        // Uzyskano: Brak wyswietlenia wiadomosci: "Umowa powinna być oznaczona do przeglądu"

        // Szef magazynu przeglada liste umow do sprawdzenia

        // Wartosc oczekiwana: lista umow, ktore powinny zostac sprawdzone oraz tym samym:
        // brak wiadomosci: "Lista umow do sprawdzenia nie powinna byc pusta" oraz
        // "Umowa z naszym id powinna byc na tej liscie" oraz "Wszystkie metody powinny byc oznaczone do sprawdzenia"
        List<RentalAgreement> agreementsToBeReviewed = rentalAgreementService.findAllRentalAgreementsToBeReviewed();
        assertFalse(agreementsToBeReviewed.isEmpty(), "Lista umow do sprawdzenia nie powinna byc pusta");
        assertTrue(agreementsToBeReviewed.stream()
                        .anyMatch(agreement -> agreement.getId().equals(agreementId)),
                "Umowa z naszym id powinna byc na tej liscie");
        assertTrue(agreementsToBeReviewed.stream().allMatch(RentalAgreement::isToBeReviewed),
                "Wszystkie metody powinny byc oznaczone do sprawdzenia");
        // Uzyskano: brak powyzszych wiadomosci

        // Sprzet uszkodzony
        // Nalozenie kary na uzytkownika z odpowiednimi parametrami

        // Okrelenie wysokości kary i opisu uszkodzenia
        FeeRequest damageFeRequest = new FeeRequest(
                FeeCategory.PENALTY,
                agreementId,
                clientId,
                testWarehouseManager.getId(),
                new BigDecimal("200.00"),
                LocalDate.now().plusDays(14),
                null,
                false
        );

        // Wartosc oczekiwana: numer oplaty oraz tym samym:
        // brak wiadomosci: "Numer oplaty nie powinien byc null"
        Integer feeId = feeService.createFee(damageFeRequest);
        assertNotNull(feeId, "Numer oplaty nie powinien byc null");
        // Uzyskano: brak wiadomosci "Numer oplaty nie powinien byc null"

        Integer toolId = testTool.getId();
        // Usuwanie sprzetu
        ToolEventRequest toolEventRequest = new ToolEventRequest(
                EventCategory.DELETION,
                null,
                toolId,
                testWarehouseManager.getId()
        );

        // Oznaczenie sprzetu jako niedostepny
        toolService.markAsUnavailable(toolId, toolEventRequest);

        // weryfikacja nalozenia kary na klienta
        // Oczekiwany wynik: oplata minus kara powinna byc wieksza od zera lub rowna oraz tym samym
        // brak wyswietlenia wiadomosci: "Klient powinien miec oplate zawierajaca kare"
        BigDecimal fee = clientRepository.getFeesById(clientId);
        assertTrue(fee.subtract(new BigDecimal("200.0")).compareTo(BigDecimal.ZERO) >= 0,
                "Klient powinien miec oplate zawierajaca kare");
        // Uzyskano brak wyswietlenia wiadomosci "Klient powinien miec oplate zawierajaca kare"

        // weryfikacja niedostepnosci sprzetu
        // Oczekiwany wynik: system powinien zwrocic null w przypadku proby pobrania niedostepnego sprzetu
        // oraz tym samym brak wiadomosci "Sprzet powinien byc null"
        Tool unaviableTool = toolService.findAvailableById(toolId);
        assertNull(unaviableTool, "Sprzet powinien byc null");
        // Uzyskano: brak wyswietlenia wiadomosci "Sprzet powinien byc null"
    }
}
