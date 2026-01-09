package com.taskmanager.TaskManagingApp.dto;

import java.time.LocalDateTime;

public record EmployeeDTO(Integer id,
                          String name,
                          String email,
                          Boolean isDeactivated,
                          LocalDateTime createdAt,
                          LocalDateTime updatedAt) { }
