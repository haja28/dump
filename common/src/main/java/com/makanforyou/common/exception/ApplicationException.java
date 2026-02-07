package com.makanforyou.common.exception;

/**
 * Base custom exception for application
 */
public class ApplicationException extends RuntimeException {
    private String errorCode;

    public ApplicationException(String message) {
        super(message);
        this.errorCode = "INTERNAL_ERROR";
    }

    public ApplicationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "INTERNAL_ERROR";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
