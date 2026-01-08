package org.bobirental.employee;

import org.bobirental.common.impl.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/employees")
public class EmployeeController extends BaseController<Employee> {

    public EmployeeController(EmployeeService employeeService) {
        super(employeeService);
    }
}
