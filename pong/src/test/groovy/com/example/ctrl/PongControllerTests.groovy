package com.example.ctrl

import org.example.PongApplication
import org.example.ctrl.PongController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.BootstrapWith
import org.springframework.test.web.reactive.server.WebTestClient
import spock.lang.Specification

@BootstrapWith(SpringBootTestContextBootstrapper)
@AutoConfigureWebTestClient
@SpringBootTest(classes = PongApplication.class, useMainMethod = SpringBootTest.UseMainMethod.ALWAYS, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PongControllerTests extends Specification {

    @Autowired
    private WebTestClient webTestClient

    def "should return 'World' when say is 'Hello'"() {
        when:
        def response = webTestClient.get()
                .uri("/pong?instance=test-instance&say=Hello")
                .accept(MediaType.TEXT_PLAIN)
                .exchange()

        then:
        response.expectStatus().isOk()
        response.expectBody(String).isEqualTo("World")
    }

    def "should return empty when say is not 'Hello'"() {
        when:
        def response = webTestClient.get()
                .uri("/pong?instance=test-instance&say=Goodbye")
                .accept(MediaType.TEXT_PLAIN)
                .exchange()

        then:
        response.expectStatus().isOk() // 由于返回 Mono.empty(), 依然返回200
        response.expectBody(String).isEqualTo(null)
    }

    def "should return 429 Too Many Requests when already processing"() {
        given:
        // First call to trigger processing
        def firstResponse = webTestClient.get()
                .uri("/pong?instance=test-instance&say=Hello")
                .accept(MediaType.TEXT_PLAIN)
                .exchange()

        // Ensure the first response is initiated
        firstResponse.expectStatus().isOk() // Ensure the first request is successful

        when:
        // Simulate a second call while the first is still processing
        def secondResponse = webTestClient.get()
                .uri("/pong?instance=test-instance&say=Hello")
                .accept(MediaType.TEXT_PLAIN)
                .exchange()

        then:
        secondResponse.expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
        //secondResponse.expectStatus().isEqualTo(HttpStatus.OK)

        // Optionally check the error message (if you implement error handling)
        secondResponse.expectBody().consumeWith { body ->
            assert body.getStatus() == HttpStatus.TOO_MANY_REQUESTS
            //assert body.getStatus() == HttpStatus.OK
        }

    }
}