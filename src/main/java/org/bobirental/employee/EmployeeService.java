package org.bobirental.employee;

import org.bobirental.common.impl.BaseService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmployeeService extends BaseService<Employee> implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        super(employeeRepository);
        this.employeeRepository = employeeRepository;
    }

    public Employee findByLogin(String login) {
        Optional<Employee> optionalEmployee = employeeRepository.findByEmployeeLogin(login);

        return optionalEmployee.orElse(null);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Employee employee = employeeRepository.findByEmployeeLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("Employee not found: + " + username));

        String role = "ROLE_" + employee.getEmployeeRole().name();

        return User.builder()
                .username(employee.getEmployeeLogin())
                .password(employee.getEmployeePassword())
                .authorities(role)
                .build();
    }
}
