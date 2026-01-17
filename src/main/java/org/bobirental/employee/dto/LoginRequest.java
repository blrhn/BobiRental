package org.bobirental.employee.dto;

public record LoginRequest(
        String employeeLogin,
        String employeePassword) { }
