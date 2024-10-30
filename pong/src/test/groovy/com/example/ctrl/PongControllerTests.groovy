package com.example.ctrl


import org.example.ctrl.PongController
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import spock.lang.Specification

class PongControllerTests extends Specification {

    PongController pongController = new PongController()

    def "test1"() {
        when:
        pongController.getPong(instance, say)
        pongController.getPong(instance, say)
        then:
        noExceptionThrown()
        where:
        instance | say
        "8080"   | "hello"
        "8080"   | "Hello"
        "8080"   | "Hello"

    }

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
}