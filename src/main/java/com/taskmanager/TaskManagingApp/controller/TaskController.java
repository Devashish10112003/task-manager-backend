package com.taskmanager.TaskManagingApp.controller;

import com.taskmanager.TaskManagingApp.dto.ApiResult;
import com.taskmanager.TaskManagingApp.dto.EmployeeDTO;
import com.taskmanager.TaskManagingApp.dto.IdResponse;
import com.taskmanager.TaskManagingApp.dto.TaskDTO;
import com.taskmanager.TaskManagingApp.models.PriorityType;
import com.taskmanager.TaskManagingApp.models.StatusType;
import com.taskmanager.TaskManagingApp.models.Employee;
import com.taskmanager.TaskManagingApp.models.Task;
import com.taskmanager.TaskManagingApp.service.AssignmentService;
import com.taskmanager.TaskManagingApp.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.taskmanager.TaskManagingApp.constants.ErrorMessages.*;


@Slf4j
@RestController
@RequestMapping("/api/v1/tasks")
@Tag(
        name = "Task Management",
        description = "APIs for creating, updating, assigning, unassigning and fetching tasks"
)
public class TaskController {

    private final TaskService taskService;
    private final AssignmentService assignmentService;

    public TaskController(TaskService taskService, AssignmentService assignmentService) {
        this.taskService = taskService;
        this.assignmentService = assignmentService;
    }

    @Operation(
            summary = "Create a new task",
            description = "Creates a task with title, description, dates, status, priority and tags"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Task created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Task Created",
                                    value = """
                                            {
                                              "id": 3
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while creating task",
                                    value = "title can't be null"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Database error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while creating employee",
                                    value = "Internal Server Error"
                            )
                    )
            )
    })

    @PostMapping("/create")
    public ResponseEntity<ApiResult<?>> createTask(
            @Parameter(description = "Task object containing task details", required = true)
            @RequestBody Task task) {
        log.info("In TaskController.createTask() title={}, priority={}, status={}", task.getTitle(), task.getPriority(), task.getStatus());

        try {
            Integer id = taskService.createTask(task.getTitle(), task.getDescription(), task.getStartDate(), task.getEndDate(), task.getTags(), task.getStatus(), task.getPriority());

            log.info("Out TaskController.createTask(), task created id={}", id);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.created(new IdResponse(id),"Task created successfully"));

        } catch (IllegalArgumentException ex) {
            log.warn("Out TaskController.createTask(), invalid input: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResult.badRequest(ex.getMessage()));

        } catch (Exception ex) {
            log.error("Out TaskController.createTask(), {},{}", INTERNAL_SERVER_ERROR, ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResult.internalServerError(INTERNAL_SERVER_ERROR+ex.getMessage()));
        }
    }

    @Operation(
            summary = "Get tasks",
            description = "Fetches all tasks or filters tasks by status or priority"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tasks fetched successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "fetched tasks successfully",
                                    value = """
                                            [
                                                {
                                                    "id": 1,
                                                    "title": "Hello",
                                                    "description": "myDesc",
                                                    "createdAt": "2026-01-07T18:11:10.404452",
                                                    "updatedAt": "2026-01-08T10:11:55.707011",
                                                    "assignedEmployeeId": null,
                                                    "startDate": "2026-01-01",
                                                    "endDate": "2026-02-02",
                                                    "tags": [],
                                                    "status": "TODO",
                                                    "priority": "HIGH",
                                                    "isDeleted": false
                                                },
                                                {
                                                    "id": 2,
                                                    "title": "Hello",
                                                    "description": "myDesc",
                                                    "createdAt": "2026-01-07T18:12:54.294032",
                                                    "updatedAt": "2026-01-08T10:11:55.707011",
                                                    "assignedEmployeeId": null,
                                                    "startDate": "2026-01-01",
                                                    "endDate": "2026-02-02",
                                                    "tags": [
                                                        "tag1",
                                                        "tag2"
                                                    ],
                                                    "status": "TODO",
                                                    "priority": "HIGH",
                                                    "isDeleted": false
                                                },
                                                {
                                                    "id": 3,
                                                    "title": "Test1",
                                                    "description": "Test1",
                                                    "createdAt": "2026-01-08T12:33:11.976668",
                                                    "updatedAt": "2026-01-08T12:33:11.976668",
                                                    "assignedEmployeeId": null,
                                                    "startDate": "2026-01-01",
                                                    "endDate": "2026-02-02",
                                                    "tags": [
                                                        "Red",
                                                        "Yellow"
                                                    ],
                                                    "status": "TODO",
                                                    "priority": "HIGH",
                                                    "isDeleted": false
                                                }
                                            ]
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Database error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while creating employee",
                                    value = "Internal Server Error"
                            )
                    )
            )
    })
    @GetMapping
    public ResponseEntity<ApiResult<?>> getTasks(
            @Parameter(description = "Filter tasks by status", example = "IN_PROGRESS")
            @RequestParam(required = false) StatusType status,
            @Parameter(description = "Filter tasks by priority", example = "HIGH")
            @RequestParam(required = false) PriorityType priority) {

        log.info("In TaskController.getTasks() status={}, priority={}", status, priority);

        try {
            if (status != null) {
                log.info("Out TaskController.getTasks(), fetching by status={}", status);
                return ResponseEntity.status(HttpStatus.OK).body(ApiResult.ok(taskService.getTaskByStatus(status),"Got tasks by status successfully"));
            }

            if (priority != null) {
                log.info("Out TaskController.getTasks(), fetching by priority={}", priority);
                return ResponseEntity.status(HttpStatus.OK).body(ApiResult.ok(taskService.getTaskByPriority(priority),"Got tasks by priority successfully"));
            }

            log.info("Out TaskController.getTasks(), fetching all tasks");
            return ResponseEntity.status(HttpStatus.OK).body(ApiResult.ok(taskService.getAllTask(),"Got tasks successfully"));

        } catch (Exception ex) {
            log.error("Out TaskController.getTasks(), {},{}", INTERNAL_SERVER_ERROR, ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResult.internalServerError(INTERNAL_SERVER_ERROR+ex.getMessage()));
        }
    }

    @Operation(
            summary = "Get task by ID",
            description = "Fetches task details for a given task ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Task found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "fetched task successfully",
                                    value = """
                                            {
                                                    "id": 10,
                                                    "title": "Hello",
                                                    "description": "myDesc",
                                                    "createdAt": "2026-01-07T18:11:10.404452",
                                                    "updatedAt": "2026-01-08T10:11:55.707011",
                                                    "assignedEmployeeId": null,
                                                    "startDate": "2026-01-01",
                                                    "endDate": "2026-02-02",
                                                    "tags": [],
                                                    "status": "TODO",
                                                    "priority": "HIGH",
                                                    "isDeleted": false
                                                }
                                            """
                            )
                    )

            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid ID",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while fetching task",
                                    value = "Task Id can't be negative"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Task not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while fetching task",
                                    value = "no task with this id 10 found"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Database error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while creating employee",
                                    value = "Internal Server Error"
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResult<?>> getTaskById(
            @Parameter(description = "Task ID", example = "10", required = true)
            @PathVariable Integer id) {
        log.info("In TaskController.getTaskById() id={}", id);

        if (id == null || id < 0) {
            log.warn("Out TaskController.getTaskById(), null id passed, id={} ", id);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResult.badRequest("Null/Negative Id passed"));
        }

        try {
            TaskDTO task = taskService.getTaskById(id);
            log.info("Out TaskController.getTaskById(), task found id={}", id);
            return ResponseEntity.status(HttpStatus.OK).body(ApiResult.ok(task,"Got task for given id successfully"));

        } catch (IllegalStateException ex) {
            log.warn("Out TaskController.getTaskById(), task not found id={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResult.notFound(String.format(TASK_NOT_FOUND,id)));

        } catch (Exception ex) {
            log.error("Out TaskController.getTaskById(), {},{}", INTERNAL_SERVER_ERROR, ex.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ApiResult.internalServerError(INTERNAL_SERVER_ERROR+ex.getMessage()));
        }
    }

    @Operation(
            summary = "Update task",
            description = "Updates task details including assignment, status and priority"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Task updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "updated task successfully",
                                    value = """
                                            {
                                                    "id": 10,
                                                    "title": "Hello",
                                                    "description": "myDesc",
                                                    "createdAt": "2026-01-07T18:11:10.404452",
                                                    "updatedAt": "2026-01-08T10:11:55.707011",
                                                    "assignedEmployeeId": 1,
                                                    "startDate": "2026-01-01",
                                                    "endDate": "2026-02-02",
                                                    "tags": [],
                                                    "status": "TODO",
                                                    "priority": "HIGH",
                                                    "isDeleted": false
                                                }
                                            """
                            )
                    )
            ),

            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while updating task",
                                    value = "end date can't be in past"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Task not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while updating task",
                                    value = "no task with id 10 found"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Database error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while creating employee",
                                    value = "Internal Server Error"
                            )
                    )
            )
    })
    @PatchMapping("/update")
    public ResponseEntity<ApiResult<?>> updateTask(
            @Parameter(description = "Task object containing updated values", required = true)
            @RequestBody Task task) {
        log.info("In TaskController.updateTask() taskId={}", task.getId());

        if (task.getId() == null || task.getId() < 0) {
            log.warn("Out TaskController.updateTask(), invalid task id={}", task.getId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResult.badRequest("Null Id passed"));
        }

        if (task.getAssignedEmployeeId() != null && task.getAssignedEmployeeId() < 0) {
            log.warn("Out TaskController.updateTask(), invalid employee id={}", task.getAssignedEmployeeId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResult.badRequest("Negative employee id passed"));
        }

        try {
            TaskDTO updatedTask = taskService.updateTask(task.getId(), task.getTitle(), task.getDescription(), task.getStartDate(), task.getEndDate(), task.getTags(), task.getStatus(), task.getPriority(), task.getAssignedEmployeeId());

            log.info("Out TaskController.updateTask(), task updated id={}", task.getId());
            return ResponseEntity.status(HttpStatus.OK).body(ApiResult.ok(updatedTask,"Updated task successfully"));

        } catch (IllegalArgumentException ex) {
            log.warn("Out TaskController.updateTask() not enough details provided, {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResult.badRequest(ex.getMessage()));

        } catch (IllegalStateException ex) {
            log.warn("Out TaskController.updateTask() no task found, {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResult.notFound(ex.getMessage()));

        } catch (Exception ex) {
            log.error("Out TaskController.updateTask(), {}, {}", INTERNAL_SERVER_ERROR, ex.getMessage());
            return ResponseEntity.status(HttpStatus.IM_USED).body(ApiResult.internalServerError( INTERNAL_SERVER_ERROR+ex.getMessage()));
        }
    }

    @Operation(
            summary = "Unassign task",
            description = "Removes the assigned employee from a task"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Task unassigned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "task unassigned successfully",
                                    value = """
                                            {
                                                    "id": 10,
                                                    "title": "Hello",
                                                    "description": "myDesc",
                                                    "createdAt": "2026-01-07T18:11:10.404452",
                                                    "updatedAt": "2026-01-08T10:11:55.707011",
                                                    "assignedEmployeeId": null,
                                                    "startDate": "2026-01-01",
                                                    "endDate": "2026-02-02",
                                                    "tags": [],
                                                    "status": "TODO",
                                                    "priority": "HIGH",
                                                    "isDeleted": false
                                                }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Task not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while unassigning task",
                                    value = "task id can't be negative"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Task not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while unassigning task",
                                    value = "no task with this id found/task already unassigned"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Database error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while creating employee",
                                    value = "Internal Server Error"
                            )
                    )
            )
    })
    @PatchMapping("/{id}/unassign")
    public ResponseEntity<ApiResult<?>> unAssignTask(
            @Parameter(description = "Task ID", example = "8", required = true)
            @PathVariable Integer id) {
        log.info("In TaskController.unAssignTask() id={}", id);

        if (id == null || id < 0) {
            log.warn("Out TaskController.unAssignTask(), invalid id={}", id);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResult.badRequest("Null Id passed"));
        }

        try {
            TaskDTO task = taskService.unAssignTask(id);
            log.info("Out TaskController.unAssignTask(), task unassigned id={}", id);
            return ResponseEntity.status(HttpStatus.OK).body(ApiResult.ok(task,"Unassigned task successfully"));

        } catch (IllegalStateException | IllegalArgumentException ex) {
            log.warn("Out TaskController.unAssignTask(), {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResult.notFound(ex.getMessage()));

        } catch (Exception ex) {
            log.error("Out TaskController.unAssignTask(), {}, {}", INTERNAL_SERVER_ERROR, ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResult.internalServerError(INTERNAL_SERVER_ERROR+ex.getMessage()));
        }
    }

    @Operation(
            summary = "Get assigned employee for a task",
            description = "Fetches the employee currently assigned to a task"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Assigned employee fetched",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "fetched assigned employee successfully",
                                    value = """
                                            {
                                            "id": 1,
                                            "name": "Test1",
                                            "email": "Test1@example.com",
                                            "isDeactivated": true,
                                            "createdAt": "2026-01-07T18:53:35.28799",
                                            "updatedAt": "2026-01-08T00:30:43.710468",
                                            "isDeleted": false
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid task ID",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while getting the assigned user",
                                    value = "task id can't be negative"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Invalid task ID/No user assigned to task",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while getting the assigned user",
                                    value = "no task with this id found"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Database error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while creating employee",
                                    value = "Internal Server Error"
                            )
                    )
            )
    })
    @GetMapping("/user/{id}")
    public ResponseEntity<ApiResult<?>> getAssignedUser(
            @Parameter(description = "Task ID", example = "4", required = true)
            @PathVariable Integer id) {
        log.info("In TaskController.getAssignedUser() taskId={}", id);

        if (id == null || id < 0) {
            log.warn("Out TaskController.getAssignedUser(), invalid task id={}", id);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResult.badRequest("No task found"));
        }

        try {
            EmployeeDTO employee = assignmentService.getAssignedUser(id);
            log.info("Out TaskController.getAssignedUser(), employee found for taskId={}", id);
            return ResponseEntity.status(HttpStatus.OK).body(ApiResult.ok(employee,"Got assigned user successfully"));

        }
        catch (IllegalStateException ex)
        {
            log.warn("Out TaskController.getAssignedUser(), {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResult.notFound(ex.getMessage()));
        }
        catch (Exception ex) {
            log.error("Out TaskController.getAssignedUser(), {},{}", INTERNAL_SERVER_ERROR, ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResult.internalServerError(INTERNAL_SERVER_ERROR+ex.getMessage()));
        }
    }

    @Operation(
            summary = "Delete a task",
            description = "Permanently deletes a task by ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Task deleted",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Deleted task successfully",
                                    value = "task with id 5 deleted"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid ID",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while getting the deleting task",
                                    value = "task id can't be negative"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Task not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while deleting task",
                                    value = "no task with this id found"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Database error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while creating employee",
                                    value = "Internal Server Error"
                            )
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResult<?>> deleteTask(
            @Parameter(description = "Task ID", example = "5", required = true)
            @PathVariable Integer id) {
        log.info("In TaskController.deleteTask(){}", id);
        if (id == null || id < 0) {
            log.warn("Out TaskController.deleteTask(), null id passed, id={} ", id);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResult.badRequest("Null/Negative Id passed"));
        }
        try {
            taskService.deleteTask(id);
            log.info("Out TaskController.deleteTask(),task deleted successfully");
            return ResponseEntity.status(HttpStatus.OK).body(ApiResult.ok(new IdResponse(id),"Task deleted successfully"));
        } catch (IllegalStateException ex) {
            log.warn("Out TaskController.deleteTask(), task with id={} not found", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResult.notFound(ex.getMessage()));
        } catch (Exception ex) {
            log.error("Out TaskController.deleteTask(), {}, {}" ,INTERNAL_SERVER_ERROR, ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResult.internalServerError(INTERNAL_SERVER_ERROR+ex.getMessage()));
        }
    }
}
