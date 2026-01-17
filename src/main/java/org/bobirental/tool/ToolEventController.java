package org.bobirental.tool;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.bobirental.common.impl.BaseController;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    @PostMapping
    public ToolEvent createEntity(@RequestBody ToolEvent entity) {
        return toolEventService.saveEntity(entity);
    }

    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    @PutMapping("/{id}")
    public ToolEvent updateEntity(@RequestBody ToolEvent entity,  @PathVariable Integer id) {
        return toolEventService.updateEntity(entity);
    }

    @GetMapping("/tool/{id}")
    @PreAuthorize("hasAnyRole('REGULAR_EMPLOYEE', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "Get tool events by tool id, descending")
    public List<ToolEvent> findToolEventByToolId(@PathVariable Integer id) {
        return toolEventService.findToolEventByToolIdDesc(id);
    }

    @GetMapping("/employee/{id}")
    @PreAuthorize("hasAnyRole('REGULAR_EMPLOYEE', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "Get tool events by employee id, descending")
    public List<ToolEvent> findToolByEmployeeId(@PathVariable Integer id) {
        return toolEventService.findToolEventByEmployeeIdDesc(id);
    }
}
