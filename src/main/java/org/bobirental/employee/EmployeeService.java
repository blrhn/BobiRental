package org.bobirental.employee;

import org.bobirental.common.impl.BaseService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService extends BaseService<Employee> {
    public EmployeeService(EmployeeRepository employeeRepository) {
        super(employeeRepository);
    }
}
