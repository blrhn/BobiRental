package org.bobirental;

import jakarta.transaction.Transactional;
import org.bobirental.employee.Employee;
import org.bobirental.employee.EmployeeRepository;
import org.bobirental.employee.EmployeeRole;
import org.bobirental.tool.*;
import org.bobirental.tool.dto.ToolEventRequest;
import org.bobirental.tool.dto.ToolRequest;
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
public class UpdatingToolIntegrationTests {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ToolRepository toolRepository;

    @Autowired
    private ToolEventRepository toolEventRepository;

    @Autowired
    private ToolEventService toolEventService;

    @Autowired
    private ToolService toolService;

    private Tool testTool;
    private Employee testWarehouseManager;

    @BeforeEach
    public void setup() {
        setTestTool();
        setTestWarehouseManager();
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

    private void setTestWarehouseManager() {
        testWarehouseManager = new Employee();
        testWarehouseManager.setName("Remigiusz");
        testWarehouseManager.setSurname("Gruby");
        testWarehouseManager.setEmployeeLogin("r.gruby");
        testWarehouseManager.setEmployeePassword("haslo123");
        testWarehouseManager.setEmployeeRole(EmployeeRole.WAREHOUSE_MANAGER);

        testWarehouseManager = employeeRepository.save(testWarehouseManager);
    }

    @Test
    public void testToolUpdate_EditTool() {
        // podanie numeru sprzetu
        Integer toolId =  testTool.getId();

        // Wyswietlenie aktualnych danych sprzetu
        // Wynik oczekiwany: przy probie pobrania sprzetu do wyswietlenia, sprzet powinien istniec
        // (brak wyrzucenia wyjatku) oraz tym samym brak wyswietlenia komunikatu: "Narzedzie powinno istniec"
        assertDoesNotThrow((() -> toolService.findEntityById(toolId)), "Narzedzie powinno istniec");
        // Uzyskano: brak wyswietlenia komunikatu: "Narzedzie powinno istniec"

        // Wybrano edycje danych
        ToolRequest toolRequest = new ToolRequest(
                "Wiertarka Frania",
                AvailabilityStatus.AVAILABLE,
                "Wiertarka do niezwykle małych śrubek",
                ToolCategory.DRILL,
                new BigDecimal("250.45") // edycja wartosci sprzetu
        );

        // Wynik oczekiwany: zaktualizowane dane narzedzia
        // oraz tym samym brak wyswietlenia komuniaktow: "Narzedzie nadal powinno istniec po aktualizacji"
        // oraz "Wartosc narzedzia powinna zostac zaktualizowana"
        Tool updatedTool = toolService.updateTool(toolRequest, toolId);

        // weryfikacja aktualizacji
        assertNotNull(updatedTool, "Narzedzie nadal powinno istniec po aktualizacji");
        assertEquals(new BigDecimal("250.45"), updatedTool.getToolPrice(), "Wartosc narzedzia powinna zostac zaktualizowana");
        // Uzyskano: brak wyswietlenia komunikatow: "Narzedzie nadal powinno istniec po aktualizacji"
        // oraz "Wartosc narzedzia powinna zostac zaktualizowana"
    }

    @Test
    public void testToolUpdate_DeleteTool() {
        // podanie numeru sprzetu
        Integer toolId =  testTool.getId();

        // Wyswietlenie aktualnych danych sprzetu
        // Wynik oczekiwany: przy probie pobrania sprzetu do wyswietlenia, sprzet powinien istniec
        // (brak wyrzucenia wyjatku) oraz tym samym brak wyswietlenia komunikatu: "Narzedzie powinno istniec"
        assertDoesNotThrow((() -> toolService.findEntityById(toolId)), "Narzedzie powinno istniec");
        // Uzyskano: brak wyswietlenia komunikatu: "Narzedzie powinno istniec"

        // Wybrano usuniecie sprzetu przez szefa magazynu
        ToolEventRequest toolEventRequest = new ToolEventRequest(
                EventCategory.DELETION,
                "Frania sie popsula :(",
                toolId,
                testWarehouseManager.getId()
        );

        // Usuniecie = oznaczenie sprzetu jako niedostepny
        // Wynik oczekiwany: zmiana statusu sprzetu w spisie magazynu
        // oraz tym samym brak wyswietlenia komunikatu: "Status sprzetu powinien byc UNAVAILABLE"
        toolService.markAsUnavailable(toolId, toolEventRequest);

        Tool removedTool = toolRepository.findById(toolId).orElseThrow();
        assertEquals(AvailabilityStatus.UNAVAILABLE, removedTool.getToolAvailabilityStatus(),
                "Status sprzetu powinien byc UNAVAILABLE");
        // Uzyskano: brak wyswietlenia komunikatu: "Status sprzetu powinien byc UNAVAILABLE"

        // Weryfikacja zdarzenia
        // oczekiwany wynik: zanotowanie zdarzenia oraz tym samym brak wyswietlenia komuniaktow:
        // "Powinno istniec zdarzenie usuniecia" oraz "Zdarzenie powinno miec odpowiedni komentarz"
        List<ToolEvent> events = toolEventService.findToolEventByToolIdDesc(toolId);
        assertFalse(events.isEmpty(), "Powinno istniec zdarzenie usuniecia");
        assertEquals(EventCategory.DELETION, events.getFirst().getEventCategory());
        assertEquals("Frania sie popsula :(", events.getFirst().getEventComment(),
                "Zdarzenie powinno miec odpowiedni komentarz");
        // Uzyskano: brak wyswietlenia komunikatow: "Powinno istniec zdarzenie usuniecia"
        // oraz "Zdarzenie powinno miec odpowiedni komentarz"
    }
}
