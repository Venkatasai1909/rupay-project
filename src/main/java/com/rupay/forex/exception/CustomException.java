package com.rupay.forex.exception;

public class CustomException extends RuntimeException {
    public String errorCode;
    public CustomException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
