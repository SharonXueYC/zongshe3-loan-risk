package com.example.zongshe1.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<Map<String, Object>> handleValidationException(
            Exception e, HttpServletRequest request) {
        log.error("参数验证异常: {} - {}", request.getRequestURI(), e.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", "VALIDATION_ERROR");
        String detail = extractValidationMessage(e);
        response.put("message", detail != null ? detail : "参数验证失败");
        response.put("timestamp", System.currentTimeMillis());
        response.put("path", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    private String extractValidationMessage(Exception e) {
        if (e instanceof MethodArgumentNotValidException ex) {
            return ex.getBindingResult().getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .filter(msg -> msg != null && !msg.isBlank())
                    .collect(Collectors.joining("；"));
        }
        if (e instanceof BindException ex) {
            return ex.getBindingResult().getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .filter(msg -> msg != null && !msg.isBlank())
                    .collect(Collectors.joining("；"));
        }
        return null;
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(
            BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: {} - {}", request.getRequestURI(), e.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", e.getCode());
        response.put("message", e.getMessage());
        response.put("timestamp", System.currentTimeMillis());
        response.put("path", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllException(
            Exception e, HttpServletRequest request) {
        log.error("系统异常: {} - {}", request.getRequestURI(), e.getMessage(), e);

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", "SYSTEM_ERROR");
        response.put("message", "系统内部错误，请稍后重试");
        response.put("timestamp", System.currentTimeMillis());
        response.put("path", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}