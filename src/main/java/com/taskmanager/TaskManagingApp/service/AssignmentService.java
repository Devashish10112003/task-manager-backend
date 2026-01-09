package com.taskmanager.TaskManagingApp.service;

import com.taskmanager.TaskManagingApp.dto.EmployeeDTO;
import com.taskmanager.TaskManagingApp.dto.TaskDTO;
import com.taskmanager.TaskManagingApp.models.Employee;
import com.taskmanager.TaskManagingApp.models.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AssignmentService {
    private final EmployeeService employeeService;
    private final TaskService taskService;

    public AssignmentService(EmployeeService employeeService, TaskService taskService) {
        this.employeeService = employeeService;
        this.taskService = taskService;
    }

    public EmployeeDTO getAssignedUser(Integer taskId) {
        log.info("In AssignmentService.getAssignedUser(), taskId={}", taskId);

        TaskDTO task = taskService.getTaskById(taskId);
        Integer assigned_employee_id=task.assignedEmployeeId();
        if(assigned_employee_id!=null)
        {
            log.info("Out AssignmentService.getAssignedUser(), found employee");
            return employeeService.getEmployeeById(assigned_employee_id);
        }
        else
        {
            log.error("Out AssignmentService.getAssignedUser(), no employee assigned to the task");
            throw new IllegalStateException("No employee assigned to the task");
        }

    }

    public List<TaskDTO> getAllTaskAssignedToEmployee(Integer employeeId) {
        log.info("In AssignmentService.getAllTaskAssignedToEmployee(), employeeId={}", employeeId);

        EmployeeDTO employee = employeeService.getEmployeeById(employeeId);
        List<TaskDTO> tasks = taskService.getAllTaskAssignedToEmployee(employee.id());

        log.info("Out AssignmentService.getAllTaskAssignedToEmployee(), employeeId={}, totalTasks={}", employeeId, tasks.size());

        return tasks;
    }
}
