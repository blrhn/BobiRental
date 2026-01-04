package org.bobirental.tool;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.bobirental.common.model.BaseEntity;
import org.bobirental.employee.Employee;

import java.time.LocalDate;

@Entity
@Table(name = "tool_event")
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "event_id")),
})
public class ToolEvent extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(name = "event_category")
    @NotNull
    private EventCategory eventCategory;

    @Column(name = "event_date")
    @NotNull
    private LocalDate eventDate;

    @Column(name = "event_comment")
    @Size(max = 300, message = "{validation.name.size.too_long}")
    private String eventComment;

    @ManyToOne
    @JoinColumn(name = "tool_id")
    private Tool tool;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    public ToolEvent() {
        this.eventDate =  LocalDate.now();
    }

    public EventCategory getEventCategory() {
        return this.eventCategory;
    }

    public void setEventCategory(EventCategory eventCategory) {
        this.eventCategory = eventCategory;
    }

    public LocalDate getEventDate() {
        return this.eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventComment() {
        return this.eventComment;
    }

    public void setEventComment(String eventComment) {
        this.eventComment = eventComment;
    }

    public Tool getTool() {
        return this.tool;
    }

    public void setTool(Tool tool) {
        this.tool = tool;
    }

    public Employee getEmployee() {
        return this.employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}
