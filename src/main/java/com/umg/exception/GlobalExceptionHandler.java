package com.umg.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("valid", false);
        body.put("executionStatus", "INVALID_REQUEST");
        body.put("message", "Error de validacion en el request.");

        List<String> details = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .toList();
        body.put("errors", details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleNotReadable(HttpMessageNotReadableException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("valid", false);
        body.put("executionStatus", "INVALID_REQUEST");

        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife) {
            String path = ife.getPath().stream()
                .map(r -> r.getFieldName())
                .filter(n -> n != null)
                .findFirst().orElse("valor");
            body.put("message", "Valor invalido para '" + path + "': " + ife.getValue());
        } else {
            body.put("message", "JSON mal formado o tipo de dato incorrecto.");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(InvalidCompilerRequestException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRequest(InvalidCompilerRequestException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("valid", false);
        body.put("executionStatus", "INVALID_REQUEST");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(UnsupportedDialectException.class)
    public ResponseEntity<Map<String, Object>> handleUnsupportedDialect(UnsupportedDialectException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("valid", false);
        body.put("executionStatus", "UNSUPPORTED_DIALECT");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("valid", false);
        body.put("executionStatus", "INTERNAL_ERROR");
        body.put("message", "Error interno del servidor: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
