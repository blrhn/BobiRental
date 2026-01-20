package org.bobirental.employee;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee mockEmployee;

    @BeforeEach
    void setUp() {
        mockEmployee = new Employee();
        mockEmployee.setId(1);
        mockEmployee.setName("Jan");
        mockEmployee.setSurname("Kowalski");
        mockEmployee.setEmployeeLogin("jan.kowalski");
        mockEmployee.setEmployeePassword("haslo123");
        mockEmployee.setEmployeeRole(EmployeeRole.REGULAR_EMPLOYEE);
    }

    @Test
    public void testFindByLogin_ShouldReturnEmployee_WhenEmployeeExists() {
        // given
        when(employeeRepository.findByEmployeeLogin("jan.kowalski")).thenReturn(Optional.of(mockEmployee));

        // when
        Employee employee = employeeService.findByLogin("jan.kowalski");

        // then
        assertNotNull(employee);
        assertEquals("jan.kowalski", employee.getEmployeeLogin());
        assertEquals(EmployeeRole.REGULAR_EMPLOYEE, employee.getEmployeeRole());
    }

    @Test
    public void testFindByLogin_ShouldReturnNull_WhenEmployeeDoesNotExist() {
        // given
        when(employeeRepository.findByEmployeeLogin("brak")).thenReturn(Optional.empty());

        // when
        Employee result = employeeService.findByLogin("brak");

        // then
        assertNull(result);
    }

    @Test
    public void testLoadUserByUsername_ShouldReturnUserDetails_WhenEmployeeExists() {
        // given
        when(employeeRepository.findByEmployeeLogin("jan.kowalski")).thenReturn(Optional.of(mockEmployee));

        // when
        UserDetails userDetails = employeeService.loadUserByUsername("jan.kowalski");

        // then
        assertNotNull(userDetails);
        assertEquals("jan.kowalski", userDetails.getUsername());
        assertEquals("haslo123", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_REGULAR_EMPLOYEE")));
    }

    @Test
    public void testLoadUserByUsername_ShouldThrowException_WhenEmployeeDoesNotExist() {
        // given
        when(employeeRepository.findByEmployeeLogin("brak")).thenReturn(Optional.empty());

        // when oraz then
        assertThrows(UsernameNotFoundException.class, () -> employeeService.loadUserByUsername("brak"));
    }
}
