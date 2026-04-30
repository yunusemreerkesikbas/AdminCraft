package com.backend.shared.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private String result; // SUCCESS or ERROR
    private String message; // success or error message
    private T data; // return object from service class, if successful
    private Integer code; // HTTP status code for errors, null for success
    /** Stable machine-readable error identifier when applicable (e.g. media upload validation). */
    private String errorCode;

    public ApiResponse(String result, String message, T data) {
        this(result, message, data, null, null);
    }

    /**
     * {@code code} is optional HTTP-style numeric metadata used by some endpoints (not the same as {@link #errorCode}).
     */
    public ApiResponse(String result, String message, T data, Integer code) {
        this(result, message, data, code, null);
    }

    public ApiResponse(String result, String message, T data, Integer code, String errorCode) {
        this.result = result;
        this.message = message;
        this.data = data;
        this.code = code;
        this.errorCode = errorCode;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("SUCCESS", message, data, null, null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", "Operation completed successfully", data, null, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("ERROR", message, null, null, null);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>("ERROR", message, null, code, null);
    }

    /**
     * Error response with stable {@code errorCode} for clients (e.g. {@code CONTENT_MISMATCH}).
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return new ApiResponse<>("ERROR", message, null, null, errorCode);
    }
}
