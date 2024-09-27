package org.example.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: chenhs
 * @date: Created in 18:53 2024/9/25
 **/


@ControllerAdvice
@RestController
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatus status = ex.getStatus();
        if (status == HttpStatus.TOO_MANY_REQUESTS) {
            //String requestId = (String) RequestContextHolder.getRequestAttributes().getAttribute("requestId", RequestAttributes.SCOPE_REQUEST);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("timestamp", LocalDateTime.now());
            errorResponse.put("path", "/ping");
            errorResponse.put("status", status.value());
            errorResponse.put("error", status.getReasonPhrase());
            errorResponse.put("message", ex.getReason());
            //errorResponse.put("requestId", requestId);

            return ResponseEntity.status(status).body(errorResponse);
        }
        throw ex;
    }
}