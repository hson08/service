package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;


@SpringBootApplication
public class PingApplication {


    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(PingApplication.class, args);
        String port = context.getEnvironment().getProperty("server.port");
        String url = "http://localhost:"+ port;

        WebClient client = WebClient.create(url);
        Flux.interval(Duration.ofSeconds(1))
                .flatMap(i -> client.get().uri("/ping?say=Hello&instance="+port).exchange())
                .flatMap(clientResponse -> clientResponse.bodyToMono(String.class))
                .subscribe(response -> System.out.println("Response from Pong: " + response));

        /*Flux.interval(Duration.ofSeconds(1))
                .flatMap(i -> Flux.merge(
                        client.get().uri("/pong?say=Hello").exchange()
                                .flatMap(clientResponse -> clientResponse.bodyToMono(String.class)),
                        client.get().uri("/pong?say=Hello").exchange()
                                .flatMap(clientResponse -> clientResponse.bodyToMono(String.class))
                ))
                .subscribe(response -> System.out.println("Response from Pong: " + response));*/
    }
}