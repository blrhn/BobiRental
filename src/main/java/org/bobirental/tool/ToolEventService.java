package org.bobirental.tool;

import jakarta.persistence.EntityNotFoundException;
import org.bobirental.common.impl.BaseService;
import org.bobirental.employee.Employee;
import org.bobirental.employee.EmployeeRepository;
import org.bobirental.tool.dto.ToolEventRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ToolEventService extends BaseService<ToolEvent> {

    private final ToolEventRepository toolEventRepository;
    private final ToolRepository toolRepository;
    private final EmployeeRepository employeeRepository;

    public ToolEventService(
            ToolEventRepository toolEventRepository,
            ToolRepository toolRepository,
            EmployeeRepository employeeRepository) {
        super(toolEventRepository);
        this.toolEventRepository = toolEventRepository;
        this.toolRepository = toolRepository;
        this.employeeRepository = employeeRepository;
    }

    public Integer createToolEvent(ToolEventRequest toolEventRequest) {
        Tool tool = toolRepository.findById(toolEventRequest.toolId()).orElseThrow(() -> new EntityNotFoundException("Tool not found"));
        Employee employee = employeeRepository.findById(toolEventRequest.employeeId()).orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        ToolEvent toolEvent = new ToolEvent();

        toolEvent.setTool(tool);
        toolEvent.setEmployee(employee);
        toolEvent.setEventComment(toolEventRequest.eventComment());
        toolEvent.setEventCategory(toolEventRequest.eventCategory());

        return toolEventRepository.save(toolEvent).getId();
    }


    public List<ToolEvent> findToolEventByToolIdDesc(Integer toolId) {
        return toolEventRepository.findToolEventByToolIdDesc(toolId);
    }

    public List<ToolEvent> findToolEventByEmployeeIdDesc(Integer employeeId) {
        return toolEventRepository.findToolEventByEmployeeIdDesc(employeeId);
    }
}
