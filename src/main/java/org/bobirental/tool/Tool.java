package org.bobirental.tool;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.bobirental.common.model.BaseEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
@Table(name = "tool")
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "tool_id")),
})
public class Tool extends BaseEntity {
    @Column(name = "tool_name")
    @NotBlank
    @Size(max = 40, message = "{validation.name.size.too_long}")
    private String toolName;

    @Enumerated(EnumType.STRING)
    @Column(name = "tool_availability_status")
    @NotNull
    private AvailabilityStatus toolAvailabilityStatus;

    @Column(name = "tool_entry_date")
    @NotNull
    private LocalDate toolEntryDate;

    @Column(name = "tool_description")
    @Size(max = 300, message = "{validation.name.size.too_long}")
    private String toolDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "tool_category")
    @NotNull
    private ToolCategory toolCategory;

    @Column(name = "tool_price")
    @NotNull
    private BigDecimal toolPrice;

    public Tool() {
        this.toolAvailabilityStatus = AvailabilityStatus.AVAILABLE;
    }

    public String getToolName() {
        return this.toolName;
    }
    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public AvailabilityStatus getToolAvailabilityStatus() {
        return this.toolAvailabilityStatus;
    }

    public void setToolAvailabilityStatus(AvailabilityStatus toolAvailabilityStatus) {
        this.toolAvailabilityStatus = toolAvailabilityStatus;
    }

    public LocalDate getToolEntryDate() {
        return this.toolEntryDate;
    }

    public void setToolEntryDate(LocalDate toolEntryDate) {
        this.toolEntryDate = toolEntryDate;
    }

    public String getToolDescription() {
        return this.toolDescription;
    }

    public void setToolDescription(String toolDescription) {
        this.toolDescription = toolDescription;
    }

    public ToolCategory getToolCategory() {
        return this.toolCategory;
    }

    public void setToolCategory(ToolCategory toolCategory) {
        this.toolCategory = toolCategory;
    }

    public BigDecimal getToolPrice() {
        return this.toolPrice.setScale(2, RoundingMode.UP);
    }

    public void setToolPrice(BigDecimal toolPrice) {
        this.toolPrice = toolPrice.setScale(2, RoundingMode.UP);
    }
}
