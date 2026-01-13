package org.bobirental.tool;

import org.bobirental.common.impl.BaseController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tool_events")
public class ToolEventController extends BaseController<ToolEvent> {

    private final ToolEventService toolEventService;

    public ToolEventController(ToolEventService toolEventService) {
        super(toolEventService);
        this.toolEventService = toolEventService;
    }

    @GetMapping("/tool/{toolId}")
    public List<ToolEvent> findToolEventByToolId(@PathVariable Integer toolId) {
        return toolEventService.findToolEventByToolIdDesc(toolId);
    }

    @GetMapping("/employee/{employeeId}")
    public List<ToolEvent> findToolByEmployeeId(@PathVariable Integer employeeId) {
        return toolEventService.findToolEventByEmployeeIdDesc(employeeId);
    }
}
