package com.campus.trading.utils;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    @Test
    void accessDeniedReturnsForbiddenBusinessResponse() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<Result<Void>> response =
                handler.handleAccessDeniedException(new AccessDeniedException("Access Denied"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(403, response.getBody().getCode());
        assertEquals("没有权限访问", response.getBody().getMessage());
        assertEquals(null, response.getBody().getData());
    }
}
