package com.campus.trading.utils;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 处理空指针异常
    @ExceptionHandler(NullPointerException.class)
    public Result<?> handleNullPointerException(NullPointerException e) {
        e.printStackTrace();
        return Result.error("数据不存在");
    }

    // 处理数据库重复键异常（如学号重复）
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<?> handleDuplicateKeyException(DuplicateKeyException e) {
        e.printStackTrace();
        return Result.error("数据已存在，请检查学号或用户名");
    }

    // 处理所有其他异常
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        e.printStackTrace();
        return Result.error("服务器内部错误：" + e.getMessage());
    }
}