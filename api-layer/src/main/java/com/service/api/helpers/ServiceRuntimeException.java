package com.service.api.helpers;

public class ServiceRuntimeException extends RuntimeException {

    private final String messageCode;

    private final transient Object[] args;

    public ServiceRuntimeException(String messageCode, Throwable throwable, Object... args) {
        super(String.format(messageCode, args), throwable);
        this.messageCode = messageCode;
        this.args = args;
    }

    public ServiceRuntimeException(String messageCode, Object... args) {
        super(String.format(messageCode, args));
        this.messageCode = messageCode;
        this.args = args;
    }

    public String getMessageCode() {
        return this.messageCode;
    }

    public Object[] getMessageArguments() {
        return this.args;
    }
}

