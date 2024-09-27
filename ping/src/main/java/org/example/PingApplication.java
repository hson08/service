package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;


@EnableDiscoveryClient
@SpringBootApplication
public class PingApplication {

    @LoadBalanced
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        SpringApplication.run(PingApplication.class, args);

        /*WebClient client = WebClient.create("http://localhost:8081");

        Flux.interval(Duration.ofSeconds(1))
                .flatMap(i -> client.get().uri("/pong").exchange())
                .flatMap(clientResponse -> clientResponse.bodyToMono(String.class))
                .subscribe(response -> System.out.println("Response from Pong: " + response));*/
    }
}