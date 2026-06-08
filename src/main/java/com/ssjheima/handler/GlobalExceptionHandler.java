package com.ssjheima.handler;

import com.ssjheima.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result handle(Exception e) {
        log.error("服务器异常", e);
        return Result.error("服务器异常，请查看后端日志或确认数据库表已创建");
    }
}

