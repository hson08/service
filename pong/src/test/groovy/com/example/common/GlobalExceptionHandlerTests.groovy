package com.example.common

import org.example.common.GlobalExceptionHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.server.ResponseStatusException
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDateTime

@Subject(GlobalExceptionHandler)
class GlobalExceptionHandlerTests extends Specification {

    GlobalExceptionHandler handler = new GlobalExceptionHandler();

    def "should handle ResponseStatusException with TOO_MANY_REQUESTS status"() {
        given:
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many requests")

        when:
        ResponseEntity<Map<String, Object>> response = handler.handleResponseStatusException(exception)

        then:
        response.statusCode == HttpStatus.TOO_MANY_REQUESTS
        response.body != null
        response.body.timestamp instanceof LocalDateTime
        response.body.path == "/ping"
        response.body.status == HttpStatus.TOO_MANY_REQUESTS.value()
        response.body.error == HttpStatus.TOO_MANY_REQUESTS.toString()
        response.body.message == "Too many requests"
    }

    def "should throw exception if status is not TOO_MANY_REQUESTS"() {
        given:
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error")

        when:
        handler.handleResponseStatusException(exception)

        then:
        thrown(ResponseStatusException)
    }
}
