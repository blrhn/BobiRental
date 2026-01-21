package org.bobirental.tool;

import org.bobirental.common.impl.BaseService;
import org.bobirental.tool.dto.ToolEventRequest;
import org.bobirental.tool.dto.ToolRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ToolService extends BaseService<Tool> {
    private final ToolRepository toolRepository;
    private final ToolEventService toolEventService;

    public ToolService(ToolRepository toolRepository, ToolEventService toolEventService) {
        super(toolRepository);
        this.toolRepository = toolRepository;
        this.toolEventService = toolEventService;
    }

    Integer saveTool(ToolRequest toolRequest) {
        Tool tool = new Tool();

        tool.setToolName(toolRequest.toolName());
        tool.setToolPrice(toolRequest.toolPrice());
        tool.setToolCategory(toolRequest.toolCategory());
        tool.setToolDescription(toolRequest.toolDescription());
        tool.setToolAvailabilityStatus(toolRequest.availabilityStatus());

        return toolRepository.save(tool).getId();
    }

    public Tool updateTool(ToolRequest toolRequest, Integer id) {
        Tool existingTool = this.findEntityById(id);

        existingTool.setToolName(toolRequest.toolName());
        existingTool.setToolPrice(toolRequest.toolPrice());
        existingTool.setToolCategory(toolRequest.toolCategory());
        existingTool.setToolDescription(toolRequest.toolDescription());
        existingTool.setToolAvailabilityStatus(toolRequest.availabilityStatus());

        return toolRepository.save(existingTool);
    }

    public void markAsUnavailable(Integer id, ToolEventRequest toolEventRequest) {
        Tool tool = this.findEntityById(id);
        tool.setToolAvailabilityStatus(AvailabilityStatus.UNAVAILABLE);
        toolRepository.save(tool);

        toolEventService.createToolEvent(toolEventRequest);
    }

    public Tool findAvailableById(Integer id) {
        return toolRepository.checkIfAvailableById(id);
    }

    public List<Tool> findAvailableByCategory(ToolCategory category) {
        return toolRepository.findAvailableToolsByCategory(category);
    }
}
