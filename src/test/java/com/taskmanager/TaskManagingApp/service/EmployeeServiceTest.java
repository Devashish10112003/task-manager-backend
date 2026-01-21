package com.taskmanager.TaskManagingApp.service;

import com.taskmanager.TaskManagingApp.dao.EmployeeDAO;
import com.taskmanager.TaskManagingApp.dto.EmployeeDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeDAO employeeDAO;

    @InjectMocks
    private EmployeeService employeeService;

    private EmployeeDTO sampleEmployee() {
        return new EmployeeDTO(
                1,
                "abc",
                "abc@example.com",
                false,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    /* ===================== CREATE ===================== */

    @Test
    void createEmployee_shouldCreateSuccessfully() {
        when(employeeDAO.existingEmployeeByEmail("abc@example.com")).thenReturn(false);
        when(employeeDAO.createEmployee("abc", "abc@example.com")).thenReturn(1);

        Integer id = employeeService.createEmployee("abc", "abc@example.com");

        assertEquals(1, id);
        verify(employeeDAO).existingEmployeeByEmail("abc@example.com");
        verify(employeeDAO).createEmployee("abc", "abc@example.com");
    }

    @Test
    void createEmployee_shouldFail_whenNameIsNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.createEmployee(null, "abc@example.com")
        );

        assertEquals("Name cannot be empty", ex.getMessage());
        verify(employeeDAO, never()).createEmployee(any(), any());
    }

    @Test
    void createEmployee_shouldFail_whenEmailIsNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.createEmployee("abc", null)
        );

        assertEquals("Email cannot be empty", ex.getMessage());
        verify(employeeDAO, never()).createEmployee(any(), any());
    }

    @Test
    void createEmployee_shouldFail_whenEmailExists() {
        when(employeeDAO.existingEmployeeByEmail("abc@example.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.createEmployee("abc", "abc@example.com")
        );

        assertEquals("Email abc@example.com already exists", ex.getMessage());
        verify(employeeDAO, never()).createEmployee(any(), any());
    }

    /* ===================== GET ===================== */
    @Test
    void getEmployee_shouldReturnEmployee(){
        Map<Integer, EmployeeDTO> mockEmployees = new HashMap<>();

        mockEmployees.put(
                1,
                new EmployeeDTO(1, "Tony Stark", "tony@stark.com", false,LocalDateTime.now(), LocalDateTime.now())
        );
        mockEmployees.put(
                2,
                new EmployeeDTO(2, "Steve Rogers", "steve@avengers.com", false,LocalDateTime.now(), LocalDateTime.now())
        );

        when(employeeDAO.findAllEmployee()).thenReturn(mockEmployees);

        Map<Integer, EmployeeDTO> result = employeeService.getEmployee();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(1));
        assertEquals("Tony Stark", result.get(1).name());

        verify(employeeDAO, times(1)).findAllEmployee();
        verifyNoMoreInteractions(employeeDAO);

    }

    /* ===================== GET BY IDS ===================== */
    @Test
    void getEmployeeByIds_shouldReturnMatchingEmployee(){
        List<Integer> ids = List.of(1, 2);
        Map<Integer, EmployeeDTO> mockEmployees = new HashMap<>();

        mockEmployees.put(
                1,
                new EmployeeDTO(1, "Tony Stark", "tony@stark.com", false,LocalDateTime.now(), LocalDateTime.now())
        );
        mockEmployees.put(
                2,
                new EmployeeDTO(2, "Steve Rogers", "steve@avengers.com", false,LocalDateTime.now(), LocalDateTime.now())
        );

        when(employeeDAO.findEmployeesByIds(ids)).thenReturn(mockEmployees);

        Map<Integer, EmployeeDTO> result = employeeService.getEmployeesByIds(ids);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(1));
        assertEquals("Tony Stark", result.get(1).name());

        verify(employeeDAO, times(1)).findEmployeesByIds(ids);
        verifyNoMoreInteractions(employeeDAO);

    }


    /* ===================== GET BY ID ===================== */

    @Test
    void getEmployeeById_shouldReturnEmployee() {
        when(employeeDAO.findEmployeeById(1)).thenReturn(sampleEmployee());

        EmployeeDTO employee = employeeService.getEmployeeById(1);

        assertNotNull(employee);
        assertEquals(1, employee.id());
        verify(employeeDAO).findEmployeeById(1);
    }

    @Test
    void getEmployeeById_shouldFail_whenNotFound() {
        when(employeeDAO.findEmployeeById(1)).thenReturn(null);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> employeeService.getEmployeeById(1)
        );

        assertEquals("No employee with 1 id found", ex.getMessage());
    }

    /* ===================== UPDATE ===================== */

    @Test
    void updateEmployee_shouldFail_whenAllFieldsNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.updateEmployeeInfo(1, null, null)
        );

        assertEquals("At least one information should be not null for updating", ex.getMessage());
    }


    @Test
    void updateEmployee_shouldFail_whenIllegalIdPassed() {

        when(employeeDAO.findEmployeeById(1)).thenReturn(null);

        IllegalStateException ex=assertThrows(
                IllegalStateException.class,
                ()->{employeeService.updateEmployeeInfo(1, "new", "new@example.com");}
        );

        assertEquals("No employee with 1 id found",ex.getMessage());
    }

    @Test
    void updateEmployee_shouldPass_withNameOnly() {
        EmployeeDTO existing = sampleEmployee();
        when(employeeDAO.findEmployeeById(1)).thenReturn(existing);
        when(employeeDAO.updateInfo(1, "new", "")).thenReturn(1);
        when(employeeDAO.findEmployeeById(1)).thenReturn(
                new EmployeeDTO(1, "new", "", false, LocalDateTime.now(), LocalDateTime.now())
        );
        EmployeeDTO updated=employeeService.updateEmployeeInfo(1, "new", "");

        assertEquals("new", updated.name());
    }

    @Test
    void updateEmployee_shouldFail_whenInvalidEmailPassed() {

        EmployeeDTO existing = sampleEmployee();

        when(employeeDAO.findEmployeeById(1)).thenReturn(existing);
        when(employeeDAO.existingEmployeeByEmail("new@example.com")).thenReturn(true);

        IllegalArgumentException ex=assertThrows(
                IllegalArgumentException.class,
                ()->{employeeService.updateEmployeeInfo(1, "new", "new@example.com");}
        );

        assertEquals("Email new@example.com already exists",ex.getMessage());
    }

    @Test
    void updateEmployee_shouldPass_withEmailOnly() {
        EmployeeDTO existing = sampleEmployee();
        EmployeeDTO updated = new EmployeeDTO(1, "", "new@example.com", false,
                LocalDateTime.now(), LocalDateTime.now());

        // Return different values on first and second calls
        when(employeeDAO.findEmployeeById(1))
                .thenReturn(existing)      // First call
                .thenReturn(updated);      // Second call (in getEmployeeById)

        when(employeeDAO.existingEmployeeByEmail("new@example.com")).thenReturn(false);
        when(employeeDAO.updateInfo(1, "", "new@example.com")).thenReturn(1);

        EmployeeDTO result = employeeService.updateEmployeeInfo(1, "", "new@example.com");

        assertEquals("new@example.com", result.email());
    }


    //Normal case everything correct
    @Test
    void updateEmployee_shouldUpdateSuccessfully() {
        EmployeeDTO existing = sampleEmployee();

        when(employeeDAO.findEmployeeById(1))
                .thenReturn(existing)
                .thenReturn(new EmployeeDTO(1, "new", "new@example.com", false, LocalDateTime.now(), LocalDateTime.now()));
        when(employeeDAO.existingEmployeeByEmail("new@example.com")).thenReturn(false);
        when(employeeDAO.updateInfo(1, "new", "new@example.com")).thenReturn(1);


        EmployeeDTO updated = employeeService.updateEmployeeInfo(1, "new", "new@example.com");

        assertEquals("new", updated.name());
        assertEquals("new@example.com", updated.email());
    }

    @Test
    void updateEmployee_shouldPass_whenEmailUnchanged() {
        EmployeeDTO existing = sampleEmployee(); // has email "abc@example.com"

        when(employeeDAO.findEmployeeById(1)).thenReturn(existing);
        // No need to mock existingEmployeeByEmail - it shouldn't be called
        when(employeeDAO.updateInfo(1, "newName", "abc@example.com")).thenReturn(1);
        when(employeeDAO.findEmployeeById(1)).thenReturn(
                new EmployeeDTO(1, "newName", "abc@example.com", false,
                        LocalDateTime.now(), LocalDateTime.now())
        );

        EmployeeDTO result = employeeService.updateEmployeeInfo(1, "newName", "abc@example.com");

        assertEquals("newName", result.name());
        assertEquals("abc@example.com", result.email());

        // Verify that email existence check was NOT called (same email)
        verify(employeeDAO, never()).existingEmployeeByEmail(anyString());
    }
    /* ===================== DEACTIVATE ===================== */

    @Test
    void deactivateEmployee_shouldDeactivateSuccessfully() {
        EmployeeDTO active = sampleEmployee();
        when(employeeDAO.findEmployeeById(1)).thenReturn(active);
        when(employeeDAO.findEmployeeById(1))
                .thenReturn(active)
                .thenReturn(new EmployeeDTO(1, "abc", "abc@example.com", true, LocalDateTime.now(), LocalDateTime.now()));

        EmployeeDTO result = employeeService.deactivateEmployee(1);

        assertTrue(result.isDeactivated());
        verify(employeeDAO).deactivateEmployee(1);
    }

    @Test
    void deactivateEmployee_shouldFail_whenNoEmployeeWithIdFound() {
        when(employeeDAO.findEmployeeById(1)).thenReturn(null);
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> employeeService.deactivateEmployee(1)
        );

        assertEquals("No employee with 1 id found",ex.getMessage());
    }

    @Test
    void deactivateEmployee_shouldFail_whenAlreadyDeactivated() {
        EmployeeDTO deactivated = new EmployeeDTO(
                1, "abc", "abc@example.com", true, LocalDateTime.now(), LocalDateTime.now()
        );

        when(employeeDAO.findEmployeeById(1)).thenReturn(deactivated);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> employeeService.deactivateEmployee(1)
        );

        assertEquals("Employee with id 1 is already deactivated", ex.getMessage());
    }

    /* ===================== ACTIVATE ===================== */

    @Test
    void activateEmployee_shouldActivateSuccessfully() {
        EmployeeDTO active = sampleEmployee();
        when(employeeDAO.findEmployeeById(1)).thenReturn(active);
        when(employeeDAO.findEmployeeById(1))
                .thenReturn(new EmployeeDTO(1, "abc", "abc@example.com", true, LocalDateTime.now(), LocalDateTime.now()))
                .thenReturn(active);

        EmployeeDTO result = employeeService.activateEmployee(1);

        assertFalse(result.isDeactivated());
        verify(employeeDAO).activateEmployee(1);
    }

    @Test
    void activateEmployee_shouldFail_whenNoEmployeeWithIdFound() {
        when(employeeDAO.findEmployeeById(1)).thenReturn(null);
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> employeeService.activateEmployee(1)
        );

        assertEquals("No employee with 1 id found",ex.getMessage());
    }

    @Test
    void activateEmployee_shouldFail_whenAlreadyActivated() {
        EmployeeDTO active=sampleEmployee();

        when(employeeDAO.findEmployeeById(1)).thenReturn(active);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> employeeService.activateEmployee(1)
        );

        assertEquals("Employee with id 1 is already activated", ex.getMessage());
    }

    /* ===================== DELETE ===================== */

    @Test
    void deleteEmployee_shouldDeleteSuccessfully() {
        when(employeeDAO.findEmployeeById(1)).thenReturn(sampleEmployee());

        employeeService.deleteEmployee(1);

        verify(employeeDAO).deleteEmployee(1);
    }

    @Test
    void deleteEmployee_shouldFail_whenNotFound() {
        when(employeeDAO.findEmployeeById(1)).thenReturn(null);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> employeeService.deleteEmployee(1)
        );

        assertEquals("No employee with 1 id found", ex.getMessage());
    }
}
