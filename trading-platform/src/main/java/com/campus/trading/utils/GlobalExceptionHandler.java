package com.campus.trading.utils;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Result<Void>> handleAccessDeniedException(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Result.error(403, "没有权限访问"));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public Result<Void> handleAiInputException(RuntimeException exception) {
        return Result.error(400, exception.getMessage());
    }

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