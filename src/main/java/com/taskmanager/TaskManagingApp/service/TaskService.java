package com.taskmanager.TaskManagingApp.service;

import com.taskmanager.TaskManagingApp.dto.EmployeeDTO;
import com.taskmanager.TaskManagingApp.dto.TaskDTO;
import com.taskmanager.TaskManagingApp.models.PriorityType;
import com.taskmanager.TaskManagingApp.models.StatusType;
import com.taskmanager.TaskManagingApp.dao.TaskDAO;
import com.taskmanager.TaskManagingApp.models.Employee;
import com.taskmanager.TaskManagingApp.models.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static com.taskmanager.TaskManagingApp.constants.ErrorMessages.*;

@Slf4j
@Service
public class TaskService {

    private final TaskDAO taskDAO;
    private final EmployeeService employeeService;

    public TaskService(TaskDAO taskDAO, EmployeeService employeeService) {
        this.taskDAO = taskDAO;
        this.employeeService = employeeService;
    }

    public Integer createTask(String title, String description, LocalDate startDate, LocalDate endDate, String[] tags, StatusType status, PriorityType priority) {
        log.info("In TaskService.createTask() title={}, status={}, priority={}", title, status, priority);

        if (title == null || title.isBlank()) {
            log.warn("Out TaskService.createTask(), title is null/blank");
            throw new IllegalArgumentException(TITLE_CANT_BE_NULL);
        }
        if (description == null || description.isBlank()) {
            log.warn("Out TaskService.createTask(), description is null/blank");
            throw new IllegalArgumentException(DESCRIPTION_CANT_BE_NULL);
        }
        if (startDate == null) {
            log.warn("Out TaskService.createTask(), startDate is null");
            throw new IllegalArgumentException(INVALID_START_DATE);
        }
        if (endDate == null || endDate.isBefore(startDate) || endDate.isBefore(LocalDate.now())) {
            log.warn("Out TaskService.createTask(), invalid endDate={}", endDate);
            throw new IllegalArgumentException(INVALID_END_DATE);
        }
        if (status == null) {
            log.warn("Out TaskService.createTask(), status is null");
            throw new IllegalArgumentException(INVALID_STATUS);
        }
        if (priority == null) {
            log.warn("Out TaskService.createTask(), priority is null");
            throw new IllegalArgumentException(INVALID_PRIORITY);
        }

        Integer id = taskDAO.createTask(title, description, startDate, endDate, tags, status, priority);

        log.info("Out TaskService.createTask(), task created id={}", id);
        return id;
    }

    public TaskDTO updateTask(Integer id, String title, String description, LocalDate startDate, LocalDate endDate, String[] tags, StatusType status, PriorityType priority, Integer employeeId) {
        log.info("In TaskService.updateTask() taskId={}", id);

        if (title == null && description == null && startDate == null && endDate == null && tags == null && status == null && priority == null && employeeId == null) {
            log.warn("Out TaskService.updateTask(), all update fields are null");
            throw new IllegalArgumentException(ALL_INFO_NULL);
        }

        TaskDTO existingTask = taskDAO.findTaskById(id);
        if (existingTask == null) {
            log.warn("Out TaskService.updateTask(), task not found id={}", id);
            throw new IllegalStateException(String.format(TASK_NOT_FOUND,id));
        }

        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            log.warn("Out TaskService.updateTask(), endDate before startDate");
            throw new IllegalArgumentException(INVALID_END_DATE);
        }

        if (endDate != null && endDate.isBefore(LocalDate.now())) {
            log.warn("Out TaskService.updateTask(), endDate in past");
            throw new IllegalArgumentException(INVALID_END_DATE);
        }

        if (employeeId != null) {
            log.info("TaskService.updateTask(), validating employeeId={}", employeeId);
            EmployeeDTO emp = employeeService.getEmployeeById(employeeId);

            if (emp.isDeactivated()) {
                log.warn("Out TaskService.updateTask(), employee deactivated id={}", employeeId);
                throw new IllegalStateException(String.format(EMPLOYEE_ALREADY_DEACTIVATED,employeeId));
            }

            taskDAO.unassignTask(id);
            log.info("TaskService.updateTask(), previous assignment removed for taskId={}", id);
        }

        int affected = taskDAO.updateTask(id, title, description, startDate, endDate, tags, status, priority, employeeId);

        log.info("TaskService.updateTask(), rows affected={}", affected);
        log.info("Out TaskService.updateTask(), task updated id={}", id);

        return taskDAO.findTaskById(id);
    }

    public TaskDTO getTaskById(Integer id) {
        log.info("In TaskService.getTaskById() id={}", id);

        TaskDTO task = taskDAO.findTaskById(id);
        if (task == null) {
            log.warn("Out TaskService.getTaskById(), task not found id={}", id);
            throw new IllegalStateException(String.format(TASK_NOT_FOUND,id));
        }

        log.info("Out TaskService.getTaskById(), task found id={}", id);
        return task;
    }

    public List<TaskDTO> getAllTask() {
        log.info("In TaskService.getAllTask()");

        List<TaskDTO> tasks = taskDAO.getAllTasks();
        log.info("Out TaskService.getAllTask(), totalTasks={}", tasks.size());

        return tasks;
    }

    public void deleteTask(Integer id) {
        log.info("In TaskService.deleteTask() id={}", id);

        TaskDTO existingTask = taskDAO.findTaskById(id);
        if (existingTask == null) {
            log.warn("Out TaskService.deleteTask(), task not found id={}", id);
            throw new IllegalStateException(String.format(TASK_NOT_FOUND,id));
        }

        taskDAO.deleteTaskById(id);
        log.info("Out TaskService.deleteTask(), task deleted id={}", id);
    }

    public List<TaskDTO> getAllTaskAssignedToEmployee(Integer employeeId) {
        log.info("In TaskService.getAllTaskAssignedToEmployee() employeeId={}", employeeId);

        List<TaskDTO> tasks = taskDAO.findTaskByEmployeeId(employeeId);
        log.info("Out TaskService.getAllTaskAssignedToEmployee(), totalTasks={}", tasks.size());

        return tasks;
    }

    public List<TaskDTO> getTaskByStatus(StatusType status) {
        log.info("In TaskService.getTaskByStatus() status={}", status);

        List<TaskDTO> tasks = taskDAO.findTasksByStatus(status);
        log.info("Out TaskService.getTaskByStatus(), totalTasks={}", tasks.size());

        return tasks;
    }

    public List<TaskDTO> getTaskByPriority(PriorityType priority) {
        log.info("In TaskService.getTaskByPriority() priority={}", priority);

        List<TaskDTO> tasks = taskDAO.findTasksByPriority(priority);
        log.info("Out TaskService.getTaskByPriority(), totalTasks={}", tasks.size());

        return tasks;
    }

    public TaskDTO unAssignTask(Integer id) {
        log.info("In TaskService.unAssignTask() taskId={}", id);

        TaskDTO existingTask = taskDAO.findTaskById(id);
        if (existingTask == null) {
            log.warn("Out TaskService.unAssignTask(), task not found id={}", id);
            throw new IllegalStateException(String.format(TASK_NOT_FOUND,id));
        }

        if (existingTask.assignedEmployeeId() == null) {
            log.warn("Out TaskService.unAssignTask(), task already unassigned id={}", id);
            throw new IllegalArgumentException("No assigned employee");
        }

        int affected = taskDAO.unassignTask(id);
        log.info("TaskService.unAssignTask(), rows affected={}", affected);

        log.info("Out TaskService.unAssignTask(), task unassigned id={}", id);
        return taskDAO.findTaskById(id);
    }
}
