package com.github.xiaotuwo.advice;

import com.alibaba.fastjson.JSON;
import com.github.xiaotuwo.dto.ResultDTO;
import com.github.xiaotuwo.exception.ErrorCode;
import com.github.xiaotuwo.exception.ErrorCodeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * Created by codedrinker on 2020/12/20.
 */
@ControllerAdvice
@Slf4j
public class CustomizeExceptionHandler {
    @ExceptionHandler(Exception.class)
    ModelAndView handle(Throwable e, HttpServletResponse response) {
        ResultDTO resultDTO;
        // 返回 JSON
        if (e instanceof ErrorCodeException) {
            resultDTO = ResultDTO.errorOf((ErrorCodeException) e);
        } else if (e instanceof HttpRequestMethodNotSupportedException) {
            resultDTO = ResultDTO.errorOf(ErrorCode.ENDPOINT_NOT_FOUND);
        } else if (e instanceof HttpMediaTypeNotSupportedException) {
            resultDTO = ResultDTO.errorOf(ErrorCode.ENDPOINT_NOT_FOUND);
        } else {
            log.error("handle error", e);
            resultDTO = ResultDTO.errorOf(ErrorCode.UNKOWN_ERROR);
        }
        try {
            response.setContentType("application/json");
            response.setStatus(200);
            response.setCharacterEncoding("utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(JSON.toJSONString(resultDTO));
            writer.close();
        } catch (IOException ioe) {
        }
        return null;
    }
}
