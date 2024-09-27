package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
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


    /**
     * 测试正常返回结果
     */
    @Test
    public void testConcurrentGetPong() {
        WebClient client = WebClient.create("http://localhost:8081");
        client.get().uri("/pong?say=Hello").exchange().flatMap(clientResponse -> clientResponse.bodyToMono(String.class))
                .subscribe(response -> System.out.println("Response from Pong: " + response));
    }

    /**
     * 测试1秒内接收的请求数
     */
    @Test
    public void testConcurrentGetPong2() {
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
