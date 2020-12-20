package com.github.xiaotuwo.exception;

/**
 * Created by codedrinker on 2020/12/20.
 */
public enum ErrorCode implements IErrorCode {
    NETWORK_ERROR(1001, "网络错误请重试"),
    UNKOWN_ERROR(1004, "未知错误请重试"),
    ENDPOINT_NOT_FOUND(1009, "参数错误或者请求类型错误"),
    FILE_UPLOAD_FAIL(1010, "文件上传失败，请重试"),
    ;

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    private Integer code;
    private String message;

    ErrorCode(Integer code, String message) {
        this.message = message;
        this.code = code;
    }
}
