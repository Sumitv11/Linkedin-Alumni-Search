package com.alumni.search.exception;

public class PhantomBusterException extends RuntimeException {

    private final String errorCode;

    public PhantomBusterException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public PhantomBusterException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
