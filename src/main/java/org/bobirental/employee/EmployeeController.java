package org.bobirental.employee;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.bobirental.common.impl.BaseController;
import org.bobirental.employee.dto.LoginRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Employees")
@RequestMapping("/employees")
public class EmployeeController extends BaseController<Employee> {

    private final EmployeeService employeeService;
    private final AuthenticationManager authenticationManager;

    public EmployeeController(EmployeeService employeeService, AuthenticationManager authenticationManager) {
        super(employeeService);
        this.employeeService = employeeService;
        this.authenticationManager = authenticationManager;
    }

    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    @PutMapping("/{id}")
    public Employee updateEntity(@RequestBody Employee entity,  @PathVariable Integer id) {
        return employeeService.updateEntity(entity);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Employee employee) {
        Employee employeeFromDb = employeeService.findByLogin(employee.getEmployeeLogin());

        if (employeeFromDb != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Employee already exists");
        }

        employeeService.saveEntity(employee);

        return ResponseEntity.ok(employee);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.employeeLogin(),
                            loginRequest.employeePassword()));

            Employee employee = employeeService.findByLogin(loginRequest.employeeLogin());

            return ResponseEntity.ok(employee);

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }
}
