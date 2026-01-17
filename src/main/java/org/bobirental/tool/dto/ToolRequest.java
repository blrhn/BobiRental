package org.bobirental.tool.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.bobirental.tool.AvailabilityStatus;
import org.bobirental.tool.ToolCategory;

import java.math.BigDecimal;

public record ToolRequest(
        @NotBlank
        String toolName,

        @NotNull
        AvailabilityStatus availabilityStatus,

        String toolDescription,

        @NotNull
        ToolCategory toolCategory,

        @NotNull
        BigDecimal toolPrice) {}
