package org.example.ctrl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * @author: chenhs
 * @date: Created in 17:27 2024/9/25
 **/
@Slf4j
@RestController
public class PongController {

    private volatile boolean isProcessingRequest = false;

    @GetMapping(value = "/pong", produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> getPong(String say) {
        if (!"Hello".equals(say)) {
            return Mono.empty();
        }

        if (isProcessingRequest) {
            /*return Mono.error(new IllegalStateException("Too Many Requests"))
                    .delayElement(Duration.ofSeconds(1))
                    .then(Mono.empty());*/
            log.info("Pong throttled it");
            return Mono.error(new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Pong throttled it"))
                    .delayElement(Duration.ofSeconds(3))
                    .then(Mono.empty());
        }

        isProcessingRequest = true;
        return Mono.just("World")
                .delayElement(Duration.ofSeconds(3))
                .doFinally(signalType -> isProcessingRequest = false);
    }
}
