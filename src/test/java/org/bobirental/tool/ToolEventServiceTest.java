package org.bobirental.tool;

import jakarta.persistence.EntityNotFoundException;
import org.bobirental.employee.Employee;
import org.bobirental.employee.EmployeeRepository;
import org.bobirental.employee.EmployeeRole;
import org.bobirental.tool.dto.ToolEventRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ToolEventServiceTest {
    @Mock
    private ToolEventRepository toolEventRepository;

    @Mock
    private ToolRepository toolRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private ToolEventService toolEventService;

    private ToolEventRequest toolEventRequest;
    private ToolEvent mockToolEvent;
    private Tool mockTool;
    private Employee mockEmployee;

    @BeforeEach
    public void setUp() {
        toolEventRequest = new ToolEventRequest(
                EventCategory.CREATION,
                "Dodanie Wiertartki Frani",
                1,
                1);

        setMockTool();
        setMockEmployee();
        mockToolEvent = setMockToolEvent(EventCategory.CREATION, 1, LocalDate.now());
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

    private void setMockEmployee() {
        mockEmployee = new Employee();
        mockEmployee.setId(1);
        mockEmployee.setName("Mariola");
        mockEmployee.setSurname("Tomaszewska");
        mockEmployee.setEmployeeRole(EmployeeRole.WAREHOUSE_MANAGER);
        mockEmployee.setEmployeeLogin("m.tomaszewska");
        mockEmployee.setEmployeePassword("haslo123");
    }

    private ToolEvent setMockToolEvent(EventCategory eventCategory, Integer id, LocalDate eventDate) {
        ToolEvent tempMockToolEvent = new ToolEvent();
        tempMockToolEvent.setEventCategory(eventCategory);
        tempMockToolEvent.setEventComment("Dodanie Wiertartki Frani");
        tempMockToolEvent.setTool(mockTool);
        tempMockToolEvent.setEmployee(mockEmployee);
        tempMockToolEvent.setId(id);
        tempMockToolEvent.setEventDate(eventDate);

        return tempMockToolEvent;
    }

    // Test utworzenia nowego ToolEvent
    @Test
    public void testCreateToolEvent_ShouldReturnId() {
        // given
        when(toolEventRepository.save(any(ToolEvent.class))).thenReturn(mockToolEvent);
        when(toolRepository.findById(1)).thenReturn(Optional.of(mockTool));
        when(employeeRepository.findById(1)).thenReturn(Optional.of(mockEmployee));

        // when
        Integer id = toolEventService.createToolEvent(toolEventRequest);

        // then
        assertNotNull(id);
        assertEquals(1, id);

        verify(toolEventRepository).save(argThat(toolEvent ->
                toolEvent.getEventCategory().equals(EventCategory.CREATION) &&
                toolEvent.getEventDate().equals(LocalDate.now())));
    }

    // Test utworzenia nowego ToolEvent gdy narzedzie nie istnieje
    @Test
    public void testCreateToolEvent_ShouldThrowException_WhenInvalidToolId() {
        // given
        ToolEventRequest tempRequest = new ToolEventRequest(
                EventCategory.CREATION,
                "Dodanie Wiertartki Frani",
                999,
                1);
        when(toolRepository.findById(999)).thenReturn(Optional.empty());

        // when oraz then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> toolEventService.createToolEvent(tempRequest));

        assertEquals("Tool not found", exception.getMessage());
        verify(toolEventRepository, never()).save(any(ToolEvent.class));
    }

    // Test pobierania tool events po numerze narzedzia chronologicznie
    @Test
    public void testFindToolEventsByToolIdDesc_ShouldReturnListOfToolEventsDesc() {
        // given
        ToolEvent anoterMockToolEvent = setMockToolEvent(EventCategory.UPDATE, 2,  LocalDate.now().plusDays(7));
        when(toolEventRepository.findToolEventByToolIdDesc(1)).thenReturn(Arrays.asList(anoterMockToolEvent, mockToolEvent));

        // when
        List<ToolEvent> toolEvents = toolEventService.findToolEventByToolIdDesc(1);

        // then
        assertNotNull(toolEvents);
        assertEquals(2, toolEvents.size());
        assertTrue(toolEvents.getFirst().getEventDate().isAfter(toolEvents.get(1).getEventDate()));

        verify(toolEventRepository).findToolEventByToolIdDesc(1);
    }

    // Test pobierania tool events po numerze pracownika chronologicznie
    @Test
    public void testFindToolEventsByEmployeeIdDesc_ShouldReturnListOfToolEventsDesc() {
        // given
        ToolEvent anoterMockToolEvent = setMockToolEvent(EventCategory.UPDATE, 2,  LocalDate.now().plusDays(7));
        when(toolEventRepository.findToolEventByEmployeeIdDesc(1)).thenReturn(Arrays.asList(anoterMockToolEvent, mockToolEvent));

        // when
        List<ToolEvent> toolEvents = toolEventService.findToolEventByEmployeeIdDesc(1);

        // then
        assertNotNull(toolEvents);
        assertEquals(2, toolEvents.size());
        assertTrue(toolEvents.getFirst().getEventDate().isAfter(toolEvents.get(1).getEventDate()));

        verify(toolEventRepository).findToolEventByEmployeeIdDesc(1);
    }


}
