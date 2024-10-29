package org.example.service

import org.example.PingApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.BootstrapWith
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.client.WebClient
import spock.lang.Specification

import java.nio.channels.FileLock
import java.nio.file.Files
import java.nio.file.Path

import static org.mockito.Mockito.*

//@SpringBootTest
@BootstrapWith(SpringBootTestContextBootstrapper)
@AutoConfigureWebTestClient
@SpringBootTest(classes = PingApplication.class, useMainMethod = SpringBootTest.UseMainMethod.ALWAYS, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PingServiceTest extends Specification {

    @Autowired
    PingService pingService

    @MockBean
    WebClient.Builder webClientBuilder // Mock WebClient Builder

    @Autowired
    WebTestClient webTestClient


    def "should send request when under limit"() {
        given:
        String instance = "test-instance"
        String say = "Hello"
        String expectedResponse = "Pong response"

        // Mock WebClient and its behavior
        WebClient mockClient = Mock(WebClient)
        def mockRequestHeadersUriSpec = Mock(WebClient.RequestHeadersUriSpec)
        def mockResponseSpec = Mock(WebClient.ResponseSpec)

        // Set up the mock chain
        when(webClientBuilder.build()).thenReturn(mockClient)
        when(mockClient.get()).thenReturn(mockRequestHeadersUriSpec)
        when(mockRequestHeadersUriSpec.uri(any())).thenReturn(mockRequestHeadersUriSpec)
        when(mockRequestHeadersUriSpec.retrieve()).thenReturn(mockResponseSpec)
        when(mockResponseSpec.bodyToMono(String.class)).thenReturn(Mono.just(expectedResponse))

        when:
        String result = pingService.callPongService(instance, say)

        then:
        result == "" // 因为方法最后返回值是空字符串
        1 * mockClient.get() // 确保调用了 WebClient.get()
    }

    def "should not send request when rate limited"() {
        given:
        String instance = "test-instance"
        String say = "Hello"

        // Simulate two requests in quick succession
        pingService.callPongService(instance, say)
        pingService.callPongService(instance, say)

        when:
        String result = pingService.callPongService(instance, say)

        then:
        result == "" // 仍然返回空字符串
        // 确保 WebClient.get() 没有被调用
        0 * webTestClient.get()
    }

    def "should reset request count after a second"() {
        given:
        String instance = "test-instance"
        String say = "Hello"

        // First call
        pingService.callPongService(instance, say)
        pingService.callPongService(instance, say)

        // Simulate waiting for more than 1 second
        Thread.sleep(1001) // 这里可以使用模拟时间的方法来避免真实等待

        when:
        String result = pingService.callPongService(instance, say)

        then:
        result == "" // 仍然返回空字符串
        1 * webTestClient.get() // 确保可以再次发送请求
    }
}