package org.example.ctrl

import org.example.PingApplication
import org.example.service.PingService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.BootstrapWith

//import org.springframework.test.web.reactive.MockMvcWebTestClient
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.servlet.client.MockMvcWebTestClient
import spock.lang.Specification
import reactor.core.publisher.Mono

import static org.springframework.test.web.reactive.server.WebTestClient.bindToServer

//@WebFluxTest(PingController)
@BootstrapWith(SpringBootTestContextBootstrapper)
@AutoConfigureWebTestClient
@SpringBootTest(classes = PingApplication.class, useMainMethod = SpringBootTest.UseMainMethod.ALWAYS, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PingControllerTest extends Specification {

    @Autowired
    PingService pingService // Mock the PingService
    @Autowired
    WebTestClient webTestClient

    def setup() {
        webTestClient = WebTestClient.bindToServer().build()
    }

    def "should return pong service result when valid parameters are provided"() {
        given:
        String instance = "test-instance"
        String say = "Hello"
        String expectedResponse = ""

        pingService.callPongService(instance, say) >> expectedResponse // Mock the service call

        when:
        def response = webTestClient.get()
                .uri("/ping?instance=${instance}&say=${say}")
                .accept(MediaType.TEXT_PLAIN)
                .exchange()

        then:
        response.expectStatus().isOk()
        response.expectBody(String).isEqualTo(expectedResponse)
    }


}
