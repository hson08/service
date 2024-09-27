package org.example;

import org.example.ctrl.PingController;
import org.example.service.PingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author: chenhs
 * @date: Created in 9:03 2024/9/26
 **/
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PingControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    /**
     * 一个线程请求1次
     */
    @Test
    public void testGetPing() {
        WebClient client = WebClient.create("http://localhost:8080");
        client.get().uri("/ping?say=Hello").exchange().flatMap(clientResponse -> clientResponse.bodyToMono(String.class))
                .subscribe(response -> System.out.println("Response from Ping: " + response));
    }

    /**
     * 同时发出两个请求，有一个请求是拒绝的
     */
    @Test
    public void testGetNoLockPing() {
        WebClient client = WebClient.create("http://localhost:8080");
        Flux.merge(
                    client.get().uri("/noLockPing?say=Hello").exchange()
                            .flatMap(clientResponse -> clientResponse.bodyToMono(String.class)),
                    client.get().uri("/noLockPing?say=Hello").exchange()
                            .flatMap(clientResponse -> clientResponse.bodyToMono(String.class))
                )
                .subscribe(response -> System.out.println("Response from Pong: " + response));
    }

    /**
     * 一个线程（单进程）请求多于2次，测试 rate limited
     */
    @Test
    public void testGetPingRateLimited() {
        int numberOfRequests = 3; // Number of concurrent requests
        Flux.range(1, numberOfRequests)
                .flatMap(i -> webTestClient.get().uri("/ping?say=Hello2")
                        .exchange()
                        .expectStatus().isOk()
                        .returnResult(String.class)
                        .getResponseBody())
                .collectList()
                .block();
    }

    /**
     * 测试 FileLock
     */
    @Test
    public void testGetPingFileLock() {
        int numberOfRequests = 2; // Number of concurrent requests
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfRequests);
        WebClient client = WebClient.create("http://localhost:8080");

        for (int i = 0; i < numberOfRequests; i++) {
            executorService.execute(() -> {
                client.get().uri("/ping?say=Hello").exchange().flatMap(clientResponse -> clientResponse.bodyToMono(String.class))
                        .subscribe(response -> System.out.println("Response from Ping: " + response));

            });
        }

        executorService.shutdown();
    }



    @InjectMocks
    private PingController pingController;

    @Mock
    private PingService pingService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetPingWithResult() {
        // Arrange
        String say = "Hello";
        String expectedResponse = "Pong response";
        when(pingService.callPongService(say)).thenReturn(expectedResponse);

        // Act
        Mono<String> result = pingController.getPing(say);

        // Assert
        StepVerifier.create(result)
                .expectNext(expectedResponse)
                .verifyComplete();
    }

    @Test
    public void testGetPingNoResult() {
        // Arrange
        String say = "Hello";
        when(pingService.callPongService(say)).thenReturn(null);

        // Act
        Mono<String> result = pingController.getPing(say);

        // Assert
        StepVerifier.create(result)
                .expectNext("pong service no result, please check the request parameters.")
                .verifyComplete();
    }

    @Test
    public void testNoLockPingWithResult() {
        // Arrange
        String say = "Hello";
        String expectedResponse = "Pong response";
        when(pingService.callPongServiceNoLock(say)).thenReturn(expectedResponse);

        // Act
        Mono<String> result = pingController.noLockPing(say);

        // Assert
        StepVerifier.create(result)
                .expectNext(expectedResponse)
                .verifyComplete();
    }

    @Test
    public void testNoLockPingNoResult() {
        // Arrange
        String say = "Hello";
        when(pingService.callPongServiceNoLock(say)).thenReturn(null);

        // Act
        Mono<String> result = pingController.noLockPing(say);

        // Assert
        StepVerifier.create(result)
                .expectNext("pong service no result, please check the request parameters.")
                .verifyComplete();
    }
}
