package com.github.xiaotuwo.exception;

/**
 * Created by codedrinker on 2020/12/20.
 */
public class ErrorCodeException extends RuntimeException {
    private String message;
    private Integer code;

    public ErrorCodeException(IErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    public ErrorCodeException(String errorMessage) {
        this.code = ErrorCode.ENDPOINT_NOT_FOUND.getCode();
        this.message = errorMessage;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public Integer getCode() {
        return code;
    }
}
