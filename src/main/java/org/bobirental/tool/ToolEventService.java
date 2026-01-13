package org.bobirental.tool;

import org.bobirental.common.impl.BaseService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ToolEventService extends BaseService<ToolEvent> {

    private final ToolEventRepository toolEventRepository;

    public ToolEventService(ToolEventRepository toolEventRepository) {
        super(toolEventRepository);
        this.toolEventRepository = toolEventRepository;
    }

    public List<ToolEvent> findToolEventByToolIdDesc(Integer toolId) {
        return toolEventRepository.findToolEventByToolIdDesc(toolId);
    }

    public List<ToolEvent> findToolEventByEmployeeIdDesc(Integer employeeId) {
        return toolEventRepository.findToolEventByEmployeeIdDesc(employeeId);
    }
}
