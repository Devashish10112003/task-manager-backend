package com.taskmanager.TaskManagingApp.service;

import com.taskmanager.TaskManagingApp.dto.EmployeeDTO;
import com.taskmanager.TaskManagingApp.dto.TaskDTO;
import com.taskmanager.TaskManagingApp.models.PriorityType;
import com.taskmanager.TaskManagingApp.models.StatusType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AssignmentServiceTest {

    @Mock
    private TaskService taskService;

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private AssignmentService assignmentService;

    private TaskDTO taskDTO;
    private EmployeeDTO employeeDTO;

    @BeforeEach
    void setup() {
         taskDTO = new TaskDTO(
                1,
                "Test Task",
                "Description",
                LocalDateTime.now(),
                LocalDateTime.now(),
                10,
                LocalDate.now(),
                LocalDate.now().plusDays(3),
                new String[]{"backend"},
                StatusType.TODO,
                PriorityType.HIGH
        );

         employeeDTO=new EmployeeDTO(
                 10,
                 "test1",
                 "test1@gmail.com",
                 false,
                 LocalDateTime.now(),
                 LocalDateTime.now()
         );
    }

    @Test
    void getAssignedUser_success(){
        when(taskService.getTaskById(1)).thenReturn(taskDTO);
        when(employeeService.getEmployeeById(10)).thenReturn(employeeDTO);

        EmployeeDTO assignedEmployee=assignmentService.getAssignedUser(1);

        assertEquals(employeeDTO.id(),assignedEmployee.id());
    }

    @Test
    void getAssignedUser_shouldFailWhenNoUserAssigned(){
        when(taskService.getTaskById(1)).thenReturn(new TaskDTO(
                1,
                "Test Task",
                "Description",
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                LocalDate.now(),
                LocalDate.now().plusDays(3),
                new String[]{"backend"},
                StatusType.TODO,
                PriorityType.HIGH
        ));
        assertThrows(IllegalStateException.class,()-> assignmentService.getAssignedUser(1));
    }

    @Test
    void getAllTaskAssignedToUser_success(){
        when(employeeService.getEmployeeById(10)).thenReturn(employeeDTO);
        when(taskService.getAllTaskAssignedToEmployee(10)).thenReturn(List.of(taskDTO));

        List<TaskDTO> result=assignmentService.getAllTaskAssignedToEmployee(10);

        assertEquals(List.of(taskDTO),result);
    }
}
