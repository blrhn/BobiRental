package org.bobirental.tool;

import org.bobirental.common.impl.BaseService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ToolService extends BaseService<Tool> {
    private final ToolRepository toolRepository;

    public ToolService(ToolRepository toolRepository) {
        super(toolRepository);
        this.toolRepository = toolRepository;
    }

    Tool findAvailableById(Integer id) {
        return toolRepository.checkIfAvailableById(id);
    }

    List<Tool> findAvailableByCategory(ToolCategory category) {
        return toolRepository.findAvailableToolsByCategory(category);
    }
}
