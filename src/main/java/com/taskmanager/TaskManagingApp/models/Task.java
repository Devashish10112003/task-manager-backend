package com.taskmanager.TaskManagingApp.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    private Integer id;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer assignedEmployeeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String[] tags;
    private StatusType status;
    private PriorityType priority;
    private Boolean isDeleted;
}
