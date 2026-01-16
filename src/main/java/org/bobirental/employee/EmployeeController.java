package org.bobirental.employee;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.bobirental.common.impl.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Employees")
@RequestMapping("/employees")
public class EmployeeController extends BaseController<Employee> {

    public EmployeeController(EmployeeService employeeService) {
        super(employeeService);
    }
}
