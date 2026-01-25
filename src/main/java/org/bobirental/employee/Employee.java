package org.bobirental.employee;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.bobirental.common.model.Person;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "employee")
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "employee_id")),
        @AttributeOverride(name = "name", column = @Column(name = "employee_name")),
        @AttributeOverride(name = "surname", column = @Column(name = "employee_surname")),
})
public class Employee extends Person {

    // Role of the employee
    @Enumerated(EnumType.STRING)
    @Column(name = "employee_role", nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @NotNull
    private EmployeeRole employeeRole;

    // Login credentials
    @Column(name = "employee_login", unique = true)
    @NotBlank
    @Size(max = 20, message = "{validation.name.size.too_long}")
    private String employeeLogin;

    @Column(name = "employee_password")
    @NotBlank
    @Size(max = 30, message = "{validation.name.size.too_long}")
    private String employeePassword;

    // Convenience method for role checking
    public boolean isWarehouseManager() {
        return this.employeeRole == EmployeeRole.WAREHOUSE_MANAGER;
    }

    // --- Getters and Setters ---
    public EmployeeRole getEmployeeRole() {
        return this.employeeRole;
    }

    public void setEmployeeRole(EmployeeRole employeeRole) {
        this.employeeRole = employeeRole;
    }

    public String getEmployeeLogin() {
        return this.employeeLogin;
    }

    public void setEmployeeLogin(String employeeLogin) {
        this.employeeLogin = employeeLogin;
    }

    public String getEmployeePassword() {
        return this.employeePassword;
    }

    public void setEmployeePassword(String employeePassword) {
        this.employeePassword = employeePassword;
    }
}
