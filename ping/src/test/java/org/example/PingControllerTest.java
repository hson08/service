package org.example;

import org.junit.jupiter.api.Test;
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
    public void testConcurrentGetPing() {
        WebClient client = WebClient.create("http://localhost:8080");
        client.get().uri("/ping?say=Hello").exchange().flatMap(clientResponse -> clientResponse.bodyToMono(String.class))
                .subscribe(response -> System.out.println("Response from Ping: " + response));
    }

    /**
     * 一个线程请求10次，测试 rate limited
     */
    @Test
    public void testConcurrentGetPing1() {
        int numberOfRequests = 10; // Number of concurrent requests
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
    public void testConcurrentGetPing2() {
        int numberOfRequests = 10; // Number of concurrent requests
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


}
