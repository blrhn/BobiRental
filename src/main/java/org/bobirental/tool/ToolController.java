package org.bobirental.tool;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.bobirental.tool.dto.ToolEventRequest;
import org.bobirental.tool.dto.ToolRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tools")
@Tag(name = "Tools")
public class ToolController {
    private final ToolService toolService;

    public ToolController(ToolService toolService) {
        this.toolService = toolService;
    }

    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    @PostMapping
    public Integer createEntity(@RequestBody ToolRequest entity) {
        return toolService.saveTool(entity);
    }

    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    @PutMapping("/{id}")
    public Tool updateEntity(@RequestBody ToolRequest entity, @PathVariable Integer id) {
        return toolService.updateTool(entity, id);
    }

    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Mark tool as unavailable instead of DELETE")
    public void deleteEntity(@PathVariable Integer id, @RequestBody ToolEventRequest eventRequest) {
        toolService.markAsUnavailable(id, eventRequest);
    }

    @GetMapping(value = "/available/{id}")
    @PreAuthorize("hasAnyRole('REGULAR_EMPLOYEE', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "Get available tool by tool id")
    public Tool findAvailableToolById(@PathVariable Integer id) {
        return toolService.findAvailableById(id);
    }

    @GetMapping(value = "/available/category/{category}")
    @PreAuthorize("hasAnyRole('REGULAR_EMPLOYEE', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "Get available tools by category")
    public List<Tool> findAvailableToolsByCategory(@PathVariable ToolCategory category) {
        return toolService.findAvailableByCategory(category);
    }

    @GetMapping(value = "get/{id}")
    @Operation(summary = "Get tool by id")
    public Tool findToolById(@PathVariable Integer id) {
        return toolService.findEntityById(id);
    }
}
