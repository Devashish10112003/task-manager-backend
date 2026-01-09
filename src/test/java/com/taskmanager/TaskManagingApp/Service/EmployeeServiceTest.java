package com.taskmanager.TaskManagingApp.Service;

import com.taskmanager.TaskManagingApp.dao.EmployeeDAO;
import com.taskmanager.TaskManagingApp.dto.EmployeeDTO;
import com.taskmanager.TaskManagingApp.service.EmployeeService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

public final class EmployeeServiceTest {
    @BeforeAll()
    public static void init() {
        EmployeeDTO employee = new EmployeeDTO(
                1,
                "abc",
                "abc@example.com",
                false,
                LocalDateTime.of(2024, 7, 21, 10, 30, 30),
                LocalDateTime.of(2024, 7, 21, 10, 30, 30)
        );
    }

    @Mock
    EmployeeDAO employeeDAO;

    @InjectMocks
    EmployeeService employeeService;


    @Test
    void createEmployeeShouldCreateSuccessfully()
    {
        when(employeeDAO.existingEmployeeByEmail("xyz")).thenReturn(false);
        when(employeeDAO.createEmployee("xyz", "xyz")).thenReturn(1);


        Integer createdEmployeeId = employeeService.createEmployee("xyz", "xyz");


        assertEquals(1, createdEmployeeId);


        verify(employeeDAO,times(1)).existingEmployeeByEmail("xyz");
        verify(employeeDAO, times(1)).createEmployee("xyz", "xyz");
    }

    @Test
    void createEmployeeShouldThrowErrorForNullEmail()
    {
        IllegalArgumentException ex= assertThrows(IllegalArgumentException.class,()->
            employeeService.createEmployee("xyz",null)
        );


        assertEquals("Email cannot be empty",ex.getMessage());


        verify(employeeDAO,never()).existingEmployeeByEmail(any());
        verify(employeeDAO,never()).createEmployee(any(),any());
    }

    @Test
    void createEmployeeShouldThrowErrorForNullName()
    {
        IllegalArgumentException ex= assertThrows(IllegalArgumentException.class,()->
                employeeService.createEmployee(null,"hey")
        );


        assertEquals("Name cannot be empty",ex.getMessage());

        verify(employeeDAO,never()).createEmployee(any(),any());
    }

    @Test
    void createEmployeeShouldThrowErrorForExistingEmail()
    {
        when(employeeDAO.existingEmployeeByEmail("xyz")).thenReturn(true);

        IllegalArgumentException ex= assertThrows(IllegalArgumentException.class,()->
                employeeService.createEmployee("xyz","xyz")
        );

        assertEquals("Email xyz already exists",ex.getMessage());
        verify(employeeDAO,times(1)).existingEmployeeByEmail(any());
        verify(employeeDAO,never()).createEmployee(any(),any());

    }

    @Test
    void getEmployeeByIdShouldReturnEmployee()
    {

    }
}
