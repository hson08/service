package org.example.ctrl

import org.example.service.PingService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import reactor.core.publisher.Mono
import spock.lang.Specification

@WebFluxTest(PingController)
class PingControllerTest extends Specification {

    @Autowired
    PingController pingController

    @MockBean
    PingService pingService // 打桩的服务

    def "should return response from PingService"() {
        given:
        String instance = "8080"
        String say = "Hello"

        // 配置打桩的行为
        pingService.callPongService(instance, say) >> "Pong response: Hello"

        when:
        Mono<String> response = pingController.getPing(instance, say)

        then:
        response.block() == "Pong response: Hello"
    }

    def "should return empty response when service returns null"() {
        given:
        String instance = "8080"
        String say = "Goodbye"

        // 配置打桩的行为返回空
        pingService.callPongService(instance, say) >> null

        when:
        Mono<String> response = pingController.getPing(instance, say)

        then:
        response.block() == null
    }


}
