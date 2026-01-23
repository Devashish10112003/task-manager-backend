package com.taskmanager.TaskManagingApp.controller;

import com.taskmanager.TaskManagingApp.dto.ApiResult;
import com.taskmanager.TaskManagingApp.dto.EmployeeDTO;
import com.taskmanager.TaskManagingApp.dto.IdResponse;
import com.taskmanager.TaskManagingApp.dto.TaskDTO;
import com.taskmanager.TaskManagingApp.models.Employee;
import com.taskmanager.TaskManagingApp.service.AssignmentService;
import com.taskmanager.TaskManagingApp.service.EmployeeService;
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

import java.util.List;
import java.util.Map;

import static com.taskmanager.TaskManagingApp.constants.ErrorMessages.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/employee")

@Tag(
        name = "Employee Management",
        description = "APIs for creating, updating, deleting employees and managing their tasks"
)
@CrossOrigin(origins = "http://localhost:5173")

public class EmployeeController {

    private final EmployeeService employeeService;
    private final AssignmentService assignmentService;

    public EmployeeController(EmployeeService employeeService, AssignmentService assignmentService) {
        this.employeeService = employeeService;
        this.assignmentService = assignmentService;
    }

    @Operation(
            summary = "Create a new employee",
            description = "Creates an employee using name and email and returns the generated employee ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Employee created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Employee Created",
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
                    description = "Invalid input provided",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while creating employee",
                                    value = "name can't be null"
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
    public ResponseEntity<ApiResult<?>> createEmployee(
            @Parameter(description = "Employee object containing name and email", required = true)
            @RequestBody Employee employee) {
        log.info("In EmployeeController.createEmployeee(){}{} Entered the create employee endpoint", employee.getName(), employee.getEmail());
        try {
            Integer id = employeeService.createEmployee(employee.getName(), employee.getEmail());
            log.info("Out EmployeeController.createEmployeee(), Created employee with id{}", id);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.created(new IdResponse(id), "Created Employee with id " + id));
        } catch (IllegalArgumentException ex) {
            log.warn("Out EmployeeController.createEmployeee(), {} ",ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResult.badRequest(ex.getMessage()));
        } catch (Exception ex) {
            log.error("Out EmpoloyeeController.createEmployee()" + INTERNAL_SERVER_ERROR + "{}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResult.internalServerError(INTERNAL_SERVER_ERROR+ex.getMessage()));
        }
    }

    @Operation(
            summary = "Get all employees",
            description = "Returns a map of all employees with employee ID as the key"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Employees fetched successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Got employees successfully",
                                    value = """
                                            {
                                                "1": {
                                                         "id": 1,
                                                         "name": "Test1",
                                                         "email": "Test1@example.com",
                                                         "isDeactivated": true,
                                                         "createdAt": "2026-01-07T18:53:35.28799",
                                                         "updatedAt": "2026-01-08T00:30:43.710468",
                                                         "isDeleted": false
                                                     },
                                                "3": {
                                                         "id": 3,
                                                         "name": "Test3",
                                                         "email": "test3@example.com",
                                                         "isDeactivated": false,
                                                         "createdAt": "2026-01-08T12:12:53.296034",
                                                         "updatedAt": "2026-01-08T12:12:53.296034",
                                                         "isDeleted": false
                                                     }
                                            }
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
                                    name = "Error while fetching employee",
                                    value = "Internal server error"
                            )
                    )
            )
    })
    @GetMapping
    public ResponseEntity<ApiResult<?>> getEmployee() {
        try {
            log.info("In EmpoloyeeController.getEmployee() ");
            Map<Integer, EmployeeDTO> employees = employeeService.getEmployee();
            log.info("Out EmpoloyeeController.getEmployee()");
            return ResponseEntity.status(HttpStatus.OK).body(ApiResult.ok(employees, "Got employee successfully"));
        } catch (Exception ex) {
            log.error("Out EmpoloyeeController.getEmployee(), {}, {}" ,INTERNAL_SERVER_ERROR, ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResult.internalServerError(INTERNAL_SERVER_ERROR+ex.getMessage()));
        }
    }

    @Operation(
            summary = "Get employee by ID",
            description = "Fetches employee details for a given employee ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Employee found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Employee found",
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
                    description = "Invalid employee ID",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while fetching employee",
                                    value = "Employee Id can't be negative"
                            )
                    )

            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Employee not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while fetching employee",
                                    value = "No Employee found with this employee Id"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Database error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while fetching employee",
                                    value = "Internal Server Error"
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResult<?>> getEmployeeById(
            @Parameter(description = "Employee ID", example = "1", required = true)
            @PathVariable Integer id) {
        log.info("In EmpoloyeeController.getEmployeeById(){}", id);
        if (id == null || id < 0) {
            log.warn("Out EmpoloyeeController.getEmployeeById(), {}, id={} ", NULL_VALUE_PASSED, id);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResult.badRequest(NULL_VALUE_PASSED));
        }
        try {
            EmployeeDTO employee = employeeService.getEmployeeById(id);
            log.info("Out EmpoloyeeController.getEmployeeById(), got employee successfully");
            return ResponseEntity.status(HttpStatus.OK).body(ApiResult.ok(employee, "Got employee with id " + id));
        } catch (IllegalStateException ex) {
            log.info("Out EmpoloyeeController.getEmployeeById(), no employee found with this id={}, {}", id,ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResult.notFound(String.format(EMPLOYEE_NOT_FOUND, id)));
        } catch (Exception ex) {
            log.error("Out EmpoloyeeController.getEmployeeById() {} {}" ,INTERNAL_SERVER_ERROR, ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResult.internalServerError(INTERNAL_SERVER_ERROR+ex.getMessage()));
        }
    }

    @Operation(
            summary = "Update employee details",
            description = "Updates name and email of an existing employee"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Employee updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Employee updated successfully",
                                    value = """
                                            {
                                            "id": 1,
                                            "name": "Test1",
                                            "email": "Test1@example.com",
                                            "isDeactivated": false,
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
                    description = "Invalid input",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while fetching employee",
                                    value = "Employee Id can't be negative"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Employee not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while fetching employee",
                                    value = "No Employee found with this employee Id"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Database error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while fetching employee",
                                    value = "Internal Server Error"
                            )
                    )
            )
    })
    @PatchMapping("/update")
    public ResponseEntity<ApiResult<?>> updateEmployee(
            @Parameter(description = "Employee object with updated details", required = true)
            @RequestBody Employee employee) {
        log.info("In EmpoloyeeController.updateEmployee(){}{}", employee.getName(), employee.getEmail());
        if (employee.getId() == null || employee.getId() < 0) {
            log.warn("Out EmpoloyeeController.updateEmployeeById(), {}, id={} ", NULL_VALUE_PASSED, employee.getId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResult.badRequest(NULL_VALUE_PASSED));
        }
        try {
            EmployeeDTO employee1 = employeeService.updateEmployeeInfo(employee.getId(), employee.getName(), employee.getEmail());
            log.info("Out EmpoloyeeController.updateEmployeeById(), employee created successfully");
            return ResponseEntity.status(HttpStatus.OK).body(ApiResult.ok(employee1, "Updated employee with id" + employee1.id()));
        } catch (IllegalArgumentException ex) {
            log.warn("Out EmpoloyeeController.updateEmployeeById(), {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResult.badRequest(ex.getMessage()));
        } catch (IllegalStateException ex) {
            log.warn("Out EmpoloyeeController.updateEmployeeById(), employee with id={} not found, {}", employee.getId(),ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResult.notFound(ex.getMessage()));
        } catch (Exception ex) {
            log.error("Out EmpoloyeeController.updateEmployee(), {}, {}" ,INTERNAL_SERVER_ERROR, ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResult.internalServerError(INTERNAL_SERVER_ERROR+ex.getMessage()));
        }
    }

    @Operation(
            summary = "Deactivate an employee",
            description = "Marks an employee as inactive instead of deleting"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Employee deactivated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Employee deactivated",
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
                    description = "Invalid ID",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while fetching employee",
                                    value = "Employee Id can't be negative"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Employee not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while fetching employee",
                                    value = "No Employee found with this employee Id"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Database error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while fetching employee",
                                    value = "Internal Server Error"
                            )
                    )
            )
    })
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResult<?>> deactivateEmployee(
            @Parameter(description = "Employee ID", example = "3", required = true)
            @PathVariable Integer id) {
        log.info("In EmpoloyeeController.deactivateEmployee(){}", id);
        if (id == null || id < 0) {
            log.warn("Out EmpoloyeeController.deactivateEmployee(),{}, id={} ", NULL_VALUE_PASSED, id);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResult.badRequest(NULL_VALUE_PASSED));
        }
        try {
            EmployeeDTO employee = employeeService.deactivateEmployee(id);
            log.info("Out EmpoloyeeController.deactivateEmployee(),employee found successfully");
            return ResponseEntity.status(HttpStatus.OK).body(ApiResult.ok(employee, "Deactivated employee with id " + id));
        } catch (IllegalStateException ex) {
            log.warn("Out EmpoloyeeController.deactivateEmployee(), employee with id={} not found, {}", id,ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResult.notFound(ex.getMessage()));
        } catch (Exception ex) {
            log.error("Out EmpoloyeeController.deactivateEmployee(), {}, {}",INTERNAL_SERVER_ERROR, ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResult.badRequest(INTERNAL_SERVER_ERROR+ex.getMessage()));
        }
    }

    @Operation(
            summary = "Activate an employee",
            description = "Marks an employee as active"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Employee activated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Employee deactivated",
                                    value = """
                                            {
                                            "id": 1,
                                            "name": "Test1",
                                            "email": "Test1@example.com",
                                            "isDeactivated": false,
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
                    description = "Invalid ID",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while activating employee",
                                    value = "Employee Id can't be negative"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Employee not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while activating employee",
                                    value = "No Employee found with this employee Id or employee already activated"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Database error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while fetching employee",
                                    value = "Internal Server Error"
                            )
                    )
            )
    })
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResult<?>> activateEmployee(
            @Parameter(description = "Employee ID", example = "3", required = true)
            @PathVariable Integer id) {
        log.info("In EmpoloyeeController.activateEmployee(){}", id);
        if (id == null || id < 0) {
            log.warn("Out EmpoloyeeController.activateEmployee(), {}, id={} ", NULL_VALUE_PASSED, id);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResult.badRequest(NULL_VALUE_PASSED));
        }
        try {
            EmployeeDTO employee = employeeService.activateEmployee(id);
            log.info("Out EmpoloyeeController.activateEmployee(),employee found successfully");
            return ResponseEntity.status(HttpStatus.OK).body(ApiResult.ok(employee, "Activated employee with id " + id));
        } catch (IllegalStateException ex) {
            log.warn("Out EmpoloyeeController.activateEmployee(), employee with id={} not found", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResult.notFound(ex.getMessage()));
        } catch (Exception ex) {
            log.error("Out EmpoloyeeController.activateEmployee()" + INTERNAL_SERVER_ERROR + "{}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResult.internalServerError(INTERNAL_SERVER_ERROR+ex.getMessage()));
        }
    }


    @Operation(
            summary = "Get tasks assigned to an employee",
            description = "Fetches all tasks assigned to a specific employee"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tasks fetched successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Task assigned to user fetched successfully",
                                    value = """
                                            [
                                                {
                                                    "id": 1,
                                                    "title": "Hello",
                                                    "description": "myDesc",
                                                    "createdAt": "2026-01-07T18:11:10.404452",
                                                    "updatedAt": "2026-01-08T10:11:55.707011",
                                                    "assignedEmployeeId": 2,
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
                                                    "assignedEmployeeId": 2,
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
                                                    "assignedEmployeeId": 2,
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
                    responseCode = "400",
                    description = "Invalid ID",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while fetching tasks",
                                    value = "Employee Id can't be negative"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Employee not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while fetching tasks",
                                    value = "No Employee found with this employee Id"
                            )
                    )),
            @ApiResponse(
                    responseCode = "503",
                    description = "Database error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while fetching tasks",
                                    value = "Internal Server Error"
                            )
                    )
            )
    })
    @GetMapping("/{id}/tasks")
    public ResponseEntity<ApiResult<?>> getAllTaskAssignedToEmployee(
            @Parameter(description = "Employee ID", example = "2", required = true)
            @PathVariable Integer id) {
        log.info("In EmpoloyeeController.getAllTaskAssignedToEmployee(){}", id);
        if (id == null || id < 0) {
            log.warn("Out EmpoloyeeController.getAllTaskAssignedToEmployee(), {}, id={} ", NULL_VALUE_PASSED, id);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResult.badRequest(NULL_VALUE_PASSED));
        }
        try {
            List<TaskDTO> tasks = assignmentService.getAllTaskAssignedToEmployee(id);
            log.info("Out EmpoloyeeController.getAllTaskAssignedToEmployee(), got All Tasks assigned to user successfully");
            return ResponseEntity.status(HttpStatus.OK).body(ApiResult.ok(tasks, "Got tasks for employee id " + id));
        } catch (IllegalStateException ex) {
            log.warn("Out EmpoloyeeController.getAllTaskAssignedToEmployee(), employee with id={} not found", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResult.notFound(ex.getMessage()));
        } catch (Exception ex) {
            log.error("Out EmpoloyeeController.getAllTaskAssignedToEmployee()" + INTERNAL_SERVER_ERROR + "{}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResult.internalServerError(INTERNAL_SERVER_ERROR+ex.getMessage()));
        }
    }

    @Operation(
            summary = "Delete an employee",
            description = "Permanently deletes an employee by ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Employee deleted",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Deleted Employee successfully",
                                    value = "Employee with id 5 deleted"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid ID",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while deleting employee ",
                                    value = "Employee Id can't be negative"
                            )
                    )
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "Employee not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while fetching tasks",
                                    value = "No Employee found with this employee Id"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Database error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while fetching tasks",
                                    value = "Internal Server Error"
                            )
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResult<?>> deleteEmployee(
            @Parameter(description = "Employee ID", example = "5", required = true)
            @PathVariable Integer id) {
        log.info("In EmpoloyeeController.deleteEmployee(){}", id);
        if (id == null || id < 0) {
            log.warn("Out EmpoloyeeController.deleteEmployee(), {}, id={} ", NULL_VALUE_PASSED, id);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResult.badRequest(NULL_VALUE_PASSED));
        }
        try {
            employeeService.deleteEmployee(id);
            log.info("Out EmpoloyeeController.deleteEmployee(),employee deleted successfully");
            return ResponseEntity.status(HttpStatus.OK).body(ApiResult.ok(new IdResponse(id), "Deleted employee with id " + id));
        } catch (IllegalStateException ex) {
            log.warn("Out EmpoloyeeController.deleteEmployee(), employee with id={} not found", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResult.notFound(ex.getMessage()));
        } catch (Exception ex) {
            log.error("Out EmpoloyeeController.deleteEmployee()" + INTERNAL_SERVER_ERROR + "{}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResult.internalServerError(INTERNAL_SERVER_ERROR+ex.getMessage()));
        }
    }

    @Operation(
            summary = "Get employees by IDs",
            description = "Fetches multiple employees using a list of employee IDs"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Employees fetched successfully",
                    content = @Content(
                            mediaType = "applcation/json",
                            examples = @ExampleObject(
                                    name = "fetched employee successfully",
                                    value = """
                                            {
                                                "1": {
                                                    "id": 1,
                                                    "name": "Test1",
                                                    "email": "Test1@example.com",
                                                    "isDeactivated": true,
                                                    "createdAt": "2026-01-07T18:53:35.28799",
                                                    "updatedAt": "2026-01-08T00:30:43.710468",
                                                    "isDeleted": false
                                                },
                                                "3": {
                                                    "id": 3,
                                                    "name": "Test3",
                                                    "email": "test3@example.com",
                                                    "isDeactivated": false,
                                                    "createdAt": "2026-01-08T12:12:53.296034",
                                                    "updatedAt": "2026-01-08T12:12:53.296034",
                                                    "isDeleted": false
                                                }
                                            }
                                            """

                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "No IDs provided",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while fetching employee",
                                    value = "No Employee found with this employee Id"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Database error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error while fetching tasks",
                                    value = "Internal Server Error"
                            )
                    )
            )
    })
    @GetMapping("/by-ids")
    public ResponseEntity<ApiResult<?>> getEmployeesByIds(
            @Parameter(description = "List of employee IDs", example = "[1,2,3]", required = true)
            @RequestParam List<Integer> ids) {
        log.info("In EmpoloyeeController.getEmployeesByIds(){}", ids);
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResult.badRequest(NULL_VALUE_PASSED));
        }

        try {
            log.info("Out EmpoloyeeController.getEmployeesByIds(), got employee successfully");
            return ResponseEntity.status(HttpStatus.OK).body(ApiResult.ok(employeeService.getEmployeesByIds(ids), "Got employees with given ids"));
        } catch (Exception ex) {
            log.error("Out EmpoloyeeController.getEmployeesByIds()" + INTERNAL_SERVER_ERROR + "{}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResult.internalServerError(INTERNAL_SERVER_ERROR+ex.getMessage()));
        }
    }
}
