package org.bobirental.tool;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.bobirental.common.impl.BaseController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tool_events")
@Tag(name = "Tool events")
public class ToolEventController extends BaseController<ToolEvent> {

    private final ToolEventService toolEventService;

    public ToolEventController(ToolEventService toolEventService) {
        super(toolEventService);
        this.toolEventService = toolEventService;
    }

    @GetMapping("/tool/{id}")
    @Operation(summary = "Get tool events by tool id, descending")
    public List<ToolEvent> findToolEventByToolId(@PathVariable Integer id) {
        return toolEventService.findToolEventByToolIdDesc(id);
    }

    @GetMapping("/employee/{id}")
    @Operation(summary = "Get tool events by employee id, descending")
    public List<ToolEvent> findToolByEmployeeId(@PathVariable Integer id) {
        return toolEventService.findToolEventByEmployeeIdDesc(id);
    }
}
