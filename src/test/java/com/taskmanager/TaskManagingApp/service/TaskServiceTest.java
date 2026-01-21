package com.taskmanager.TaskManagingApp.service;

import com.taskmanager.TaskManagingApp.dao.TaskDAO;
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

import static com.taskmanager.TaskManagingApp.constants.ErrorMessages.INVALID_END_DATE;
import static com.taskmanager.TaskManagingApp.constants.ErrorMessages.TASK_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskDAO taskDAO;

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private TaskService taskService;

    private TaskDTO taskDTO;

    @BeforeEach
    void setup() {
        taskDTO = new TaskDTO(
                1,
                "Test Task",
                "Description",
                LocalDateTime.now(),
                LocalDateTime.now(),
                1,
                LocalDate.now(),
                LocalDate.now().plusDays(3),
                new String[]{"backend"},
                StatusType.TODO,
                PriorityType.HIGH
        );
    }

    // ---------------- CREATE TASK ----------------

    @Test
    void createTask_success() {
        when(taskDAO.createTask(
                anyString(),
                anyString(),
                any(),
                any(),
                any(),
                any(),
                any()
        )).thenReturn(1);

        Integer id = taskService.createTask(
                "Task",
                "Desc",
                LocalDate.now(),
                LocalDate.now().plusDays(2),
                null,
                StatusType.TODO,
                PriorityType.HIGH
        );

        assertEquals(1, id);
        verify(taskDAO, times(1)).createTask(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void createTask_shouldThrow_whenTitleNull() {
        assertThrows(IllegalArgumentException.class, () ->
                taskService.createTask(
                        null,
                        "Desc",
                        LocalDate.now(),
                        LocalDate.now().plusDays(1),
                        null,
                        StatusType.TODO,
                        PriorityType.HIGH
                )
        );
    }

    @Test
    void createTask_shouldThrow_whenTitleEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                taskService.createTask(
                        "",
                        "Desc",
                        LocalDate.now(),
                        LocalDate.now().plusDays(1),
                        null,
                        StatusType.TODO,
                        PriorityType.HIGH
                )
        );
    }

    @Test
    void createTask_shouldThrow_whenDescriptioneNull() {
        assertThrows(IllegalArgumentException.class, () ->
                taskService.createTask(
                        "Title",
                        null,
                        LocalDate.now(),
                        LocalDate.now().plusDays(1),
                        null,
                        StatusType.TODO,
                        PriorityType.HIGH
                )
        );
    }

    @Test
    void createTask_shouldThrow_whenDescriptioneEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                taskService.createTask(
                        "Title",
                        "",
                        LocalDate.now(),
                        LocalDate.now().plusDays(1),
                        null,
                        StatusType.TODO,
                        PriorityType.HIGH
                )
        );
    }

    @Test
    void createTask_shouldThrow_whenStartDateNull() {
        assertThrows(IllegalArgumentException.class, () ->
                taskService.createTask(
                        "title",
                        "Desc",
                        null,
                        LocalDate.now().plusDays(1),
                        null,
                        StatusType.TODO,
                        PriorityType.HIGH
                )
        );
    }

    //end date null
    @Test
    void createTask_shouldThrow_whenEndDateNull() {
        assertThrows(IllegalArgumentException.class, () ->
                taskService.createTask(
                        "title",
                        "Desc",
                        LocalDate.now(),
                        null,
                        null,
                        StatusType.TODO,
                        PriorityType.HIGH
                )
        );
    }

    //end date before start date
    @Test
    void createTask_shouldThrow_whenEndDateBeforeStartDate() {

        LocalDate startDate=LocalDate.now().plusDays(10);
        LocalDate endDate=startDate.minusDays(5);
        assertThrows(IllegalArgumentException.class, () ->
                taskService.createTask(
                        "title",
                        "Desc",
                        startDate,
                        endDate,
                        null,
                        StatusType.TODO,
                        PriorityType.HIGH
                )
        );
    }

    //end date in past
    @Test
    void createTask_shouldThrow_whenEndDateInPast() {
        LocalDate startDate=LocalDate.now().minusDays(10);
        LocalDate endDate=LocalDate.now().minusDays(5);
        assertThrows(IllegalArgumentException.class, () ->
                taskService.createTask(
                        "title",
                        "Desc",
                        startDate,
                        endDate,
                        null,
                        StatusType.TODO,
                        PriorityType.HIGH
                )
        );
    }

    @Test
    void createTask_shouldThrow_whenStatusNull() {
        assertThrows(IllegalArgumentException.class, () ->
                taskService.createTask(
                        "Title",
                        "Desc",
                        LocalDate.now(),
                        LocalDate.now().plusDays(1),
                        null,
                        null,
                        PriorityType.HIGH
                )
        );
    }

    @Test
    void createTask_shouldThrow_whenPriorityNull() {
        assertThrows(IllegalArgumentException.class, () ->
                taskService.createTask(
                        "Title",
                        "Desc",
                        LocalDate.now(),
                        LocalDate.now().plusDays(1),
                        null,
                        StatusType.TODO,
                        null
                )
        );
    }

    // ---------------- GET TASK ----------------

    @Test
    void getTaskById_success() {
        when(taskDAO.findTaskById(1)).thenReturn(taskDTO);

        TaskDTO result = taskService.getTaskById(1);

        assertNotNull(result);
        assertEquals(1, result.id());
    }

    @Test
    void getTaskById_shouldThrow_whenNotFound() {
        when(taskDAO.findTaskById(1)).thenReturn(null);

        assertThrows(IllegalStateException.class, () ->
                taskService.getTaskById(1)
        );
    }

    // ---------------- UPDATE TASK ----------------

    @Test
    void updateTask_success_withEmployeeAssignment() {
        EmployeeDTO employee = new EmployeeDTO(10, "Tony", "tony@stark.com", false,LocalDateTime.now(),LocalDateTime.now());

        when(taskDAO.findTaskById(1)).thenReturn(taskDTO);
        when(employeeService.getEmployeeById(10)).thenReturn(employee);
        when(taskDAO.updateTask(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(1);
        when(taskDAO.findTaskById(1)).thenReturn(taskDTO);

        TaskDTO updated = taskService.updateTask(
                1,
                "title",
                null,
                null,
                null,
                null,
                null,
                null,
                10
        );

        assertNotNull(updated);
        verify(taskDAO).unassignTask(1);
    }


    @Test
    void updateTask_shouldFail_whenEmployeeIsDeactivated() {
        EmployeeDTO employee = new EmployeeDTO(10, "Tony", "tony@stark.com", true,LocalDateTime.now(),LocalDateTime.now());

        when(taskDAO.findTaskById(1)).thenReturn(taskDTO);
        when(employeeService.getEmployeeById(10)).thenReturn(employee);
        assertThrows(IllegalStateException.class, () ->
                taskService.updateTask(
                        1,
                        "Title",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        10
                )
        );
    }

    @Test
    void updateTask_shouldThrow_whenTaskNotFound() {
        when(taskDAO.findTaskById(1)).thenReturn(null);

        assertThrows(IllegalStateException.class, () ->
                taskService.updateTask(
                        1,
                        "Title",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                )
        );
    }

    @Test
    void updateTask_shouldThrow_whenAllFieldsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                taskService.updateTask(
                        1,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                )
        );
    }

    @Test
    void updateTask_shouldThrow_whenEndDateBeforeStartDate() {

        when(taskDAO.findTaskById(1)).thenReturn(taskDTO);

        LocalDate startDate = LocalDate.now().plusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(2);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.updateTask(
                        1,
                        null,
                        null,
                        startDate,
                        endDate,
                        null,
                        null,
                        null,
                        null
                )
        );

        assertEquals(INVALID_END_DATE, ex.getMessage());

        verify(taskDAO, never()).updateTask(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void updateTask_shouldThrow_whenEndDateInPast() {

        when(taskDAO.findTaskById(1)).thenReturn(taskDTO);
        LocalDate startDate=LocalDate.now().minusDays(10);
        LocalDate pastEndDate = LocalDate.now().minusDays(1);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.updateTask(
                        1,
                        null,
                        null,
                        startDate,
                        pastEndDate,
                        null,
                        null,
                        null,
                        null
                )
        );

        assertEquals(INVALID_END_DATE, ex.getMessage());

        verify(taskDAO, never()).updateTask(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }


    // ---------------- GET ALL TASKS ----------------

    @Test
    void getAllTask_success() {
        when(taskDAO.getAllTasks()).thenReturn(List.of(taskDTO));

        List<TaskDTO> tasks = taskService.getAllTask();

        assertEquals(1, tasks.size());
    }

    // ---------------- FILTERS ----------------

    @Test
    void getTaskByStatus_success() {
        when(taskDAO.findTasksByStatus(StatusType.TODO))
                .thenReturn(List.of(taskDTO));

        List<TaskDTO> tasks = taskService.getTaskByStatus(StatusType.TODO);

        assertEquals(1, tasks.size());
    }

    @Test
    void getTaskByPriority_success() {
        when(taskDAO.findTasksByPriority(PriorityType.HIGH))
                .thenReturn(List.of(taskDTO));

        List<TaskDTO> tasks = taskService.getTaskByPriority(PriorityType.HIGH);

        assertEquals(1, tasks.size());
    }

    @Test
    void getAllTaskAssignedToEmployee_shouldReturnTasks() {

        // given
        Integer employeeId = 10;

        List<TaskDTO> mockTasks = List.of(
                new TaskDTO(
                        1,
                        "Task 1",
                        "Desc 1",
                        null,
                        null,
                        10,
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                new TaskDTO(
                        2,
                        "Task 2",
                        "Desc 2",
                        null,
                        null,
                        10,
                        null,
                        null,
                        null,
                        null,
                        null
                )
        );

        when(taskDAO.findTaskByEmployeeId(employeeId))
                .thenReturn(mockTasks);

        // when
        List<TaskDTO> result =
                taskService.getAllTaskAssignedToEmployee(employeeId);

        // then (state)
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(10, result.getFirst().assignedEmployeeId());
        assertEquals("Task 1", result.get(0).title());

        // then (behavior)
        verify(taskDAO).findTaskByEmployeeId(employeeId);
    }


    // ---------------- UNASSIGN TASK ----------------

    @Test
    void unAssignTask_success() {
        //when(taskDAO.findTaskById(1)).thenReturn(taskDTO);
        when(taskDAO.unassignTask(1)).thenReturn(1);
        when(taskDAO.findTaskById(1)).thenReturn(
                new TaskDTO(
                        1,
                        "Test Task",
                        "Desc",
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        1,
                        LocalDate.now(),
                        LocalDate.now().plusDays(3),
                        null,
                        StatusType.TODO,
                        PriorityType.HIGH

                )
        );

        TaskDTO result = taskService.unAssignTask(1);

        assertNotNull(result);
    }

    @Test
    void unAssignTask_shouldThrow_whenAlreadyUnassigned() {
        TaskDTO unassignedTask = new TaskDTO(
                1,
                "Task",
                "Desc",
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                LocalDate.now(),
                LocalDate.now().plusDays(2),
                null,
                StatusType.TODO,
                PriorityType.HIGH
        );

        when(taskDAO.findTaskById(1)).thenReturn(unassignedTask);

        assertThrows(IllegalArgumentException.class, () ->
                taskService.unAssignTask(1)
        );
    }

    @Test
    void unAssignTask_shouldThrow_whenIllegalIdPassed() {
        when(taskDAO.findTaskById(1)).thenReturn(null);

        assertThrows(IllegalStateException.class, () ->
                taskService.unAssignTask(1)
        );
    }


    @Test
    void deleteTask_shouldDeleteSuccessfully() {
        when(taskDAO.findTaskById(1)).thenReturn(taskDTO);

        taskService.deleteTask(1);

        verify(taskDAO).deleteTaskById(1);
    }

    @Test
    void deleteEmployee_shouldFail_whenNotFound() {
        when(taskDAO.findTaskById(1)).thenReturn(null);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> taskService.deleteTask(1)
        );

        assertEquals(String.format(TASK_NOT_FOUND,1), ex.getMessage());
    }
}
