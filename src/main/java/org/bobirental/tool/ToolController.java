package org.bobirental.tool;

import org.bobirental.common.impl.BaseController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tools")
public class ToolController extends BaseController<Tool> {
    private final ToolService toolService;

    public ToolController(ToolService toolService) {
        super(toolService);
        this.toolService = toolService;
    }

    @GetMapping(value = "/available", params = "id")
    public Tool findAvailableToolById(@RequestParam Integer id) {
        return toolService.findAvailableById(id);
    }

    @GetMapping(value = "/available", params = "category")
    public List<Tool> findAvailableToolsByCategory(@RequestParam ToolCategory category) {
        return toolService.findAvailableByCategory(category);
    }
}
