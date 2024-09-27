package org.example;

import org.example.ctrl.PongController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import reactor.test.StepVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author: chenhs
 * @date: Created in 13:15 2024/9/27
 **/
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PongControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    /**
     * 测试正常返回结果
     */
    @Test
    public void testConcurrentGetPong() {
        webTestClient.get().uri("/pong?say=Hello")
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class)
                .getResponseBody()
                .doOnNext(response -> System.out.println("Response: " + response))
                .blockFirst();
    }

    @Test
    public void testConcurrentGetPong2() {
        webTestClient.get().uri("/pong?say=Hello2")
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class)
                .getResponseBody()
                .doOnNext(response -> System.out.println("Response: " + response))
                .blockFirst();
    }

    /**
     * 测试1秒内接收的请求数
     */
    @Test
    public void testConcurrentGetPong3() {
        int numberOfRequests = 10; // Number of concurrent requests
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfRequests);
        WebClient client = WebClient.create("http://localhost:8081");

        for (int i = 0; i < numberOfRequests; i++) {
            executorService.execute(() -> {
                client.get().uri("/pong?say=Hello").exchange().flatMap(clientResponse -> clientResponse.bodyToMono(String.class))
                        .subscribe(response -> System.out.println("Response from Pong: " + response));

            });
        }

        executorService.shutdown();
    }


    @InjectMocks
    private PongController pongController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetPongThrottled() {
        // Arrange
        pongController.getPong("Hello").subscribe(); // First request to set isProcessingRequest to true

        // Act
        Mono<String> result = pongController.getPong("Hello");

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException &&
                                ((ResponseStatusException) throwable).getStatus() == HttpStatus.TOO_MANY_REQUESTS &&
                                "Pong throttled it".equals(((ResponseStatusException) throwable).getReason()))
                .verify();
    }

/*
    @Autowired
    private WebTestClient webTestClient;

    private String sendRequest() {
        return webTestClient.get().uri("/pong?say=Hello")
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class)
                .getResponseBody()
                .timeout(Duration.ofSeconds(5))
                .blockFirst();
    }

    @Test
    public void testConcurrentGetPong3() {
        Flux<String> requests = Flux.range(1, 10) // 10 requests
                .flatMap(i -> Mono.fromCallable(() -> sendRequest()))
                .doOnNext(response -> System.out.println("Response: " + response))
                .collectList()
                .flatMapMany(Flux::fromIterable);

        requests.collectList().block();
    }*/

}
