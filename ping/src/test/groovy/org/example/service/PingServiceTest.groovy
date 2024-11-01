package org.example.service

import spock.lang.Specification

class PingServiceTest extends Specification {

    PingService pingService = new PingService()
    String instance = "8080"
    String say = "Hello"

    def "test"() {
        when:
        pingService.callPongService(instance, say, true)
        //pingService.callPongService(instance, say)
        then:
        //2 * pingService.callPongService(instance, say)
        noExceptionThrown()
    }

    def "test2"() {
        when:
        pingService.callPongService(instance, say, false)
        //pingService.callPongService(instance, say)
        then:
        //2 * pingService.callPongService(instance, say)
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

    /*def "test3"() {
        given:
        def LOCK_FILE = "ping.lock"
        def lockFile = new File(LOCK_FILE)
        def fos = new FileOutputStream(lockFile)
        fos.getChannel().lock()

        when:
        pingService.callPongService(instance, say)
        then:
        noExceptionThrown()
    }*/

}