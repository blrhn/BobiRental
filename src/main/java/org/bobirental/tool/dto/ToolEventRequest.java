package org.bobirental.tool.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.bobirental.tool.EventCategory;

public record ToolEventRequest(
        @NotBlank
        EventCategory eventCategory,

        String eventComment,

        @NotNull
        Integer toolId,

        @NotNull
        Integer employeeId
) {}