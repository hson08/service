package org.example.service


import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.web.reactive.function.client.WebClient
import spock.lang.Specification

@SpringBootTest
class PingServiceTest extends Specification {

    @MockBean
    WebClient.Builder webClientBuilder // 模拟 WebClient 的构建器

    PingService pingService

    def setup() {
        pingService = new PingService()
    }

    def "should send request and receive response"() {
        given:
        String instance = "8080"
        String say = "Hello"

        WebClient mockWebClient = Mock(WebClient)
        WebClient.RequestHeadersUriSpec mockRequestHeadersUriSpec = Mock(WebClient.RequestHeadersUriSpec)
        WebClient.RequestHeadersSpec mockRequestHeadersSpec = Mock(WebClient.RequestHeadersSpec)
        WebClient.ResponseSpec mockResponseSpec = Mock(WebClient.ResponseSpec)

        // 配置模拟 WebClient 的行为
        webClientBuilder.build() >> mockWebClient
        mockWebClient.get() >> mockRequestHeadersUriSpec
        mockRequestHeadersUriSpec.uri(_) >> mockRequestHeadersSpec
        mockRequestHeadersSpec.retrieve() >> mockResponseSpec
        mockResponseSpec.bodyToMono(String.class) >> Mono.just("Pong response")

        when:
        String response = pingService.callPongService(instance, say)

        then:
        response == "Pong response"
    }

    def "should handle rate limiting"() {
        given:
        String instance = "8080"
        String say = "Hello"

        // 模拟请求计数达到限制
        PingService.requestCount = 2

        when:
        String response = pingService.callPongService(instance, say)

        then:
        response == ""
        1 * pingService.log.info("{} Request not sent as being \"rate limited\"", instance)
    }

    def "should handle exceptions gracefully"() {
        given:
        String instance = "8080"
        String say = "Hello"

        WebClient mockWebClient = Mock(WebClient)
        WebClient.RequestHeadersUriSpec mockRequestHeadersUriSpec = Mock(WebClient.RequestHeadersUriSpec)
        WebClient.RequestHeadersSpec mockRequestHeadersSpec = Mock(WebClient.RequestHeadersSpec)
        WebClient.ResponseSpec mockResponseSpec = Mock(WebClient.ResponseSpec)

        // 配置模拟 WebClient 的行为抛出异常
        webClientBuilder.build() >> mockWebClient
        mockWebClient.get() >> mockRequestHeadersUriSpec
        mockRequestHeadersUriSpec.uri(_) >> mockRequestHeadersSpec
        mockRequestHeadersSpec.retrieve() >> mockResponseSpec
        mockResponseSpec.bodyToMono(String.class) >> Mono.error(new RuntimeException("Network error"))

        when:
        String response = pingService.callPongService(instance, say)

        then:
        response == ""
        // 可以验证日志或其他处理逻辑
    }
}