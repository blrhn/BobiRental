package org.bobirental.employee;

import org.bobirental.common.impl.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends BaseRepository<Employee> {
    Optional<Employee> findByEmployeeLogin(String employeeLogin);
}
