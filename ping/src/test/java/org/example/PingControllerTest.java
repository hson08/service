package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PingControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    /**
     * 一个线程（单进程）请求多于2次，测试 rate limited
     */
    @Test
    public void testGetPingRateLimited() {
        int numberOfRequests = 3; // Number of concurrent requests
        Flux.range(1, numberOfRequests)
                .flatMap(i -> webTestClient.get().uri("/ping?instance=8080&say=Hello")
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
        String instance = "8080";
        String say = "Hello";
        int numberOfRequests = 2; // Number of concurrent requests
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfRequests);
        WebClient client = WebClient.builder()
                .baseUrl("http://localhost:8080")
                .build();

        for (int i = 0; i < numberOfRequests; i++) {
            executorService.execute(() -> {
                client.get()
                        .uri("/ping?instance={instance}&say={say}", instance, say)
                        .exchange().flatMap(clientResponse -> clientResponse.bodyToMono(String.class))
                        .subscribe(response -> System.out.println("Response from Ping: " + response));

            });
        }

        executorService.shutdown();
    }
}