package org.example.ctrl

import org.example.service.PingService
import spock.lang.Specification

class PingControllerTest extends Specification {

    def pingService = Mock(PingService)
    def pingController = new PingController(pingService)

    def "should return response from PingService"() {
        given:
        def instance = "8080"
        def say = "Hello"

        pingService.callPongService(instance,say) >> "Pong response: Hello"

        when:
        def response = pingController.getPing(instance,say)

        then:
        response.block() == "Pong response: Hello"
    }


}
