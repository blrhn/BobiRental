package org.bobirental.employee.dto;

public class LoginResponse {
    private Integer id;
    private String username;
    private String role;

    public LoginResponse(Integer id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public Integer getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}
