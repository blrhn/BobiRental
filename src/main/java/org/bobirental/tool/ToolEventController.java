package org.bobirental.tool;

import org.bobirental.common.impl.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tool_events")
public class ToolEventController extends BaseController<ToolEvent> {
    public ToolEventController(ToolEventService toolEventService) {
        super(toolEventService);
    }
}
