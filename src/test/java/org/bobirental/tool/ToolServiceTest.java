package org.bobirental.tool;

import org.bobirental.tool.dto.ToolEventRequest;
import org.bobirental.tool.dto.ToolRequest;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ToolServiceTest {
    @Mock
    private ToolRepository toolRepository;

    @Mock
    private ToolEventService toolEventService;

    @InjectMocks
    private ToolService toolService;

    private Tool mockTool;
    private ToolRequest toolRequest;

    @BeforeEach
    public void setUp() {
        toolRequest = new ToolRequest(
                "Wiertarka Frania",
                AvailabilityStatus.AVAILABLE,
                "Wiertarka do drobnych śrubek",
                ToolCategory.DRILL,
                new BigDecimal("650.50"));

        setMockTool();
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

    // Test zapisu narzedzia
    @Test
    public void testSaveTool_ShouldReturnId() {
        // given
        when(toolRepository.save(any(Tool.class))).thenReturn(mockTool);

        // when
        Integer id =  toolService.saveTool(toolRequest);

        // then sprawdzenie poprawnosci id
        assertNotNull(id);
        assertEquals(1, id);

        // weryfikacja wywolania metody save z odpowiednimi parametrami
        verify(toolRepository).save(argThat(tool ->
                tool.getToolName().equals("Wiertarka Frania") &&
                tool.getToolPrice().equals(new BigDecimal("650.50")) &&
                tool.getToolCategory() == ToolCategory.DRILL &&
                tool.getToolAvailabilityStatus() == AvailabilityStatus.AVAILABLE));


    }

    // Test aktualizowania narzedzia
    @Test
    public void testUpdateTool_ShouldUpdateAllFields() {
        // given
        ToolRequest tempToolRequest = new ToolRequest(
                "Wiertarka Kaczora Donalda",
                AvailabilityStatus.AVAILABLE,
                "Wiertarka do dużych śrubek",
                ToolCategory.DRILL,
                new BigDecimal("850.50"));

        when(toolRepository.findById(1)).thenReturn(Optional.of(mockTool));
        when(toolRepository.save(any(Tool.class))).thenReturn(mockTool);

        // when
        Tool updatedTool = toolService.updateTool(tempToolRequest, 1);

        // then sprawdzenie poprawnosci zaktualizowanego narzedzie
        assertNotNull(updatedTool);
        assertEquals("Wiertarka Kaczora Donalda", updatedTool.getToolName());

        // weryfikacja wywolania metody save z odpowiednimi argumentami
        verify(toolRepository).save(argThat(tool ->
                tool.getToolName().equals("Wiertarka Kaczora Donalda") &&
                tool.getToolPrice().equals(new BigDecimal("850.50"))));
    }

    // Test oznaczania sprzetu jako niedostepnego
    @Test
    public void testMarkAsUnavailable_ShouldChangeStatusAndCreateToolEvent() {
        // given
        ToolEventRequest toolEventRequest = new ToolEventRequest(
                EventCategory.DELETION,
                "Uszkodzony silnik",
                1,
                1);

        when(toolRepository.findById(1)).thenReturn(Optional.of(mockTool));
        when(toolRepository.save(any(Tool.class))).thenReturn(mockTool);
        when(toolEventService.createToolEvent(any(ToolEventRequest.class))).thenReturn(1);

        // when
        toolService.markAsUnavailable(1, toolEventRequest);

        // then sprawdzenie czy wywolywane metody wykonaly sie z odpowiednimi parametrami
        verify(toolRepository).save(argThat(tool ->
                tool.getToolAvailabilityStatus() == AvailabilityStatus.UNAVAILABLE
        ));
        verify(toolEventService).createToolEvent(toolEventRequest);
    }

    // Test sprawdzenia dostepnosci sprzetu po id
    @Test
    public void testFindAvailableById_ShouldReturnAvailableTool() {
        // given
        when(toolRepository.checkIfAvailableById(1)).thenReturn(mockTool);

        // when
        Tool foundTool = toolService.findAvailableById(1);

        // then
        assertNotNull(foundTool);
        assertEquals(AvailabilityStatus.AVAILABLE, foundTool.getToolAvailabilityStatus());

        verify(toolRepository).checkIfAvailableById(1);
    }

    // Test pobierania listy dostepnych sprzetow po kategorii
    @Test
    public void testFindAvaiablebycategoryshouldreturnlistofavailabletool() {
        // given
        when(toolRepository.findAvailableToolsByCategory(ToolCategory.DRILL))
                .thenReturn(Collections.singletonList(mockTool));

        // when
        List<Tool> foundTools = toolService.findAvailableByCategory(ToolCategory.DRILL);

        // then
        assertNotNull(foundTools);
        assertEquals(AvailabilityStatus.AVAILABLE, foundTools.getFirst().getToolAvailabilityStatus());

        verify(toolRepository).findAvailableToolsByCategory(ToolCategory.DRILL);
    }
}
