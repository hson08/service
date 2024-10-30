package org.example.service


import org.springframework.web.reactive.function.client.WebClient
import spock.lang.Specification

class PingServiceTest extends Specification {


    PingService pingService = new PingService()

    String instance = "8080"
    String say = "Hello"

    def "test"() {
        when:
        pingService.callPongService(instance, say)
        then:
        noExceptionThrown()
    }

    /*def "test2"() {
        given:
        PingService.requestCount = 2
        when:
        pingService.callPongService(instance, say)
        then:
        noExceptionThrown()
    }*/

    def "test3"() {
        given:
        def LOCK_FILE_1 = "lock1.lock"
        def lockFile1 = new File(LOCK_FILE_1)
        def fos = new FileOutputStream(lockFile1)
        fos.getChannel().lock()

        when:
        pingService.callPongService(instance, say)
        then:
        noExceptionThrown()
    }
}