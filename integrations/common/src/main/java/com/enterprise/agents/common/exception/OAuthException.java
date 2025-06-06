package com.enterprise.agents.common.exception;

public class OAuthException extends RuntimeException {
    private final String error;
    private final String errorDescription;

    public OAuthException(String error, String errorDescription) {
        super(errorDescription);
        this.error = error;
        this.errorDescription = errorDescription;
    }

    public OAuthException(String error, String errorDescription, Throwable cause) {
        super(errorDescription, cause);
        this.error = error;
        this.errorDescription = errorDescription;
    }

    public String getError() {
        return error;
    }

    public String getErrorDescription() {
        return errorDescription;
    }
} 