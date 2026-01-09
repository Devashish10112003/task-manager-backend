package com.taskmanager.TaskManagingApp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResult<T> {
    private final Integer status;
    private final Boolean success;
    private final T data;
    private final String message;


    private ApiResult(Integer status, Boolean success, T data, String message) {
        this.status = status;
        this.success = success;
        this.data = data;
        this.message = message;
    }

    public static <T> ApiResult<T> ok(T data, String message) {
        return new ApiResult<>(200, true, data, message);
    }

    public static <T> ApiResult<T> created(T data, String message) {return new ApiResult<>(201, true, data, message);}

    public static <T> ApiResult<T> badRequest(String error) {
        return new ApiResult<>(400, false, null, error);
    }

    public static <T> ApiResult<T> notFound(String error) {
        return new ApiResult<>(404, false, null, error);
    }

    public static <T> ApiResult<T> internalServerError(String error) {return new ApiResult<>(500, false, null, error);}
}
