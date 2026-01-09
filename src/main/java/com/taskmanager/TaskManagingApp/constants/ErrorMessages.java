package com.taskmanager.TaskManagingApp.constants;

public final class ErrorMessages {

    private ErrorMessages() {
        // prevent instantiation
    }

    // Employee errors
    public static final String EMPLOYEE_NOT_FOUND =
            "No employee with %s id found";

    public static final String EMPLOYEE_ALREADY_DEACTIVATED =
            "Employee with id %s is already deactivated";

    public static final String EMPLOYEE_ALREADY_ACTIVATED =
            "Employee with id %s is already activated";


    public static final String EMAIL_ALREADY_EXISTS =
            "Email %s already exists";

    public static final String NAME_CANT_BE_NULL =
            "Name cannot be empty";

    public static final String EMAIL_CANT_BE_NULL =
            "Email cannot be empty";


    // Task errors
    public static final String TITLE_CANT_BE_NULL =
            "Task title cannot be empty";

    public static final String DESCRIPTION_CANT_BE_NULL =
            "Task description cannot be empty";

    public static final String INVALID_START_DATE =
            "Start date can't be null";

    public static final String INVALID_END_DATE =
            "End data can't be null or in past or before start date";

    public static final String NULL_VALUE_PASSED=
            "Null/Negative id passed";

    public static final String TASK_NOT_FOUND =
            "Task with id %s not found";

    public static final String INVALID_STATUS =
            "Invalid task status";

    public static final String INVALID_PRIORITY =
            "Invalid task priority";

    // Generic errors
    public static final String INTERNAL_SERVER_ERROR =
            "Server error";

    public static final String ALL_INFO_NULL =
            "At least one information should be not null for updating";
}

