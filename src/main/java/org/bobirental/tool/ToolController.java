package org.bobirental.tool;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.bobirental.common.impl.BaseController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tools")
@Tag(name = "Tools")
public class ToolController extends BaseController<Tool> {
    private final ToolService toolService;

    public ToolController(ToolService toolService) {
        super(toolService);
        this.toolService = toolService;
    }

    @Override
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    @PostMapping
    public Tool createEntity(@RequestBody Tool entity) {
        return toolService.saveEntity(entity);
    }

    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    @PutMapping("/{id}")
    public Tool updateEntity(@RequestBody Tool entity,  @PathVariable Integer id) {
        return toolService.updateEntity(entity);
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
}
