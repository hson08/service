package com.example.ctrl

import org.example.PongApplication
import org.example.ctrl.PongController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.BootstrapWith
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import spock.lang.Specification

@BootstrapWith(SpringBootTestContextBootstrapper)
@AutoConfigureWebTestClient
@SpringBootTest(classes = PongApplication.class, useMainMethod = SpringBootTest.UseMainMethod.ALWAYS, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PongControllerTests extends Specification {

    PongController pongController = new PongController()

    def "should return 'World' when 'say' is 'Hello'"() {
        given:
        String instance = "8080"
        String say = "Hello"

        and:
        // 打桩 getPong 方法返回固定值
        pongController = Stub(PongController) {
            getPong(instance, say) >> Mono.just("World")
        }

        when:
        Mono<String> response = pongController.getPong(instance, say)

        then:
        response.block() == "World"
    }

    def "should return empty when 'say' is not 'Hello'"() {
        given:
        String instance = "8080"
        String say = "Goodbye"

        and:
        // 打桩 getPong 方法返回空
        pongController = Stub(PongController) {
            getPong(instance, say) >> Mono.empty()
        }

        when:
        Mono<String> response = pongController.getPong(instance, say)

        then:
        response.block() == null
    }

    def "should throttle requests"() {
        given:
        String instance = "8080"
        String say = "Hello"

        and:
        // 模拟第一次请求
        pongController = Stub(PongController) {
            getPong(instance, say) >> Mono.just("World")
        }

        when:
        def response1 = pongController.getPong(instance, say).block()

        // 模拟第二次请求
        pongController = Stub(PongController) {
            getPong(instance, say) >> Mono.error(new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Pong throttled it"))
        }

        def response2 = pongController.getPong(instance, say).block()

        then:
        response1 == "World"
        response2 instanceof ResponseStatusException
        response2.statusCode == HttpStatus.TOO_MANY_REQUESTS
    }
}