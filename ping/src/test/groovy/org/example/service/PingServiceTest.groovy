package org.example.service


import org.springframework.web.reactive.function.client.WebClient
import spock.lang.Specification

class PingServiceTest extends Specification {

    WebClient.Builder webClientBuilder // 模拟 WebClient 的构建器

    PingService pingService = new PingService()

    String instance = "8080"
    String say = "Hello"

    def "test"() {
        when:
        pingService.callPongService(instance, say)
        then:
        noExceptionThrown()
    }

    def "test2"() {
        given:
        PingService.requestCount = 2
        when:
        pingService.callPongService(instance, say)
        then:
        noExceptionThrown()
    }

    def "test3"() {
        given:
        def LOCK_FILE_PATH = "ping.lock"
        def lockFile = new File(LOCK_FILE_PATH)
        def fos = new FileOutputStream(lockFile)
        fos.getChannel().lock()

        when:
        pingService.callPongService(instance, say)
        then:
        noExceptionThrown()
    }
}