package com.taskmanager.TaskManagingApp.dto;

import com.taskmanager.TaskManagingApp.models.PriorityType;
import com.taskmanager.TaskManagingApp.models.StatusType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskDTO( Integer id,
         String title,
         String description,
         LocalDateTime createdAt,
         LocalDateTime updatedAt,
         Integer assignedEmployeeId,
         LocalDate startDate,
         LocalDate endDate,
         String[] tags,
         StatusType status,
         PriorityType priority) {
}
