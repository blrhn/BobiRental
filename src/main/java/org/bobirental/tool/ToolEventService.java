package org.bobirental.tool;

import org.bobirental.common.impl.BaseService;
import org.springframework.stereotype.Service;

@Service
public class ToolEventService extends BaseService<ToolEvent> {
    public ToolEventService(ToolEventRepository toolEventRepository) {
        super(toolEventRepository);
    }
}
