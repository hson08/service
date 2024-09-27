package org.example;

import org.example.common.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author: chenhs
 * @date: Created in 4:42 2024/9/28
 **/
@SpringBootTest
@AutoConfigureMockMvc
public class GlobalExceptionHandlerTest {
    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    public void testHandleResponseStatusExceptionTooManyRequests() {
        // Arrange
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many requests");

        // Act
        ResponseEntity<Map<String, Object>> responseEntity = globalExceptionHandler.handleResponseStatusException(exception);

        // Assert
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().get("status")).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(responseEntity.getBody().get("error")).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase());
        assertThat(responseEntity.getBody().get("message")).isEqualTo("Too many requests");
    }

    @Test
    public void testHandleResponseStatusExceptionOtherStatus() {
        // Arrange
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad request");

        // Act & Assert
        try {
            globalExceptionHandler.handleResponseStatusException(exception);
        } catch (ResponseStatusException e) {
            assertThat(e.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

}
