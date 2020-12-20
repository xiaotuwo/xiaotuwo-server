package com.github.xiaotuwo.dto;

import com.github.xiaotuwo.exception.ErrorCode;
import com.github.xiaotuwo.exception.ErrorCodeException;
import lombok.Data;

/**
 * Created by codedrinker on 2020/12/20.
 */
@Data
public class ResultDTO<T> {
    private Integer code;
    private String message;
    private boolean success;
    private T data;

    public static ResultDTO errorOf(Integer code, String message) {
        ResultDTO resultDTO = new ResultDTO();
        resultDTO.setCode(code);
        resultDTO.setMessage(message);
        return resultDTO;
    }

    public static ResultDTO errorOf(String message) {
        ResultDTO resultDTO = new ResultDTO();
        resultDTO.setCode(ErrorCode.NETWORK_ERROR.getCode());
        resultDTO.setMessage(message);
        return resultDTO;
    }

    public static ResultDTO errorOf(ErrorCode errorCode) {
        return errorOf(errorCode.getCode(), errorCode.getMessage());
    }

    public static ResultDTO errorOf(ErrorCodeException e) {
        return errorOf(e.getCode(), e.getMessage());
    }

    public static ResultDTO okOf() {
        ResultDTO resultDTO = new ResultDTO();
        resultDTO.setCode(200);
        resultDTO.setMessage("请求成功");
        resultDTO.setSuccess(true);
        return resultDTO;
    }

    public static <T> ResultDTO okOf(T t) {
        ResultDTO resultDTO = new ResultDTO();
        resultDTO.setCode(200);
        resultDTO.setMessage("请求成功");
        resultDTO.setSuccess(true);
        resultDTO.setData(t);
        return resultDTO;
    }
}
