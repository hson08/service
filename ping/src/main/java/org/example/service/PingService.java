package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.time.Duration;
import java.time.Instant;


@Slf4j
@Service
public class PingService {
    public static final String LOCK_FILE_PATH = "ping.lock";
    File lockFile = new File(LOCK_FILE_PATH);
    public static int requestCount = 0;
    public static Instant lastRequestTime = Instant.now();


    public String callPongService(String instance, String say) {
        String res = "";
        try (FileOutputStream fos = new FileOutputStream(lockFile)) {
            FileLock lock = fos.getChannel().lock();

            if (durationSeconds() >= 1) {
                requestCount = 0;
                lastRequestTime = Instant.now();
            }

            if (requestCount < 2) {
                log.info("{} Request sent: {}", instance, say);
                requestCount++;
                call(instance, say);

            } else {
                log.info("{} Request not send as being \"rate limited\"", instance);
            }
            lock.release();
        } catch (OverlappingFileLockException e) {
            log.error("Caught OverlappingFileLockException: {}", e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return res;
    }

    public void call(String instance, String say) {
        WebClient client = WebClient.create("http://localhost:8081");
        client.get()
                .uri(uriBuilder ->
                        uriBuilder.path("/pong")
                                .queryParam("instance", instance)
                                .queryParam("say", say)
                                .build())
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(response -> {
                    log.info("{} Pong Respond: {}", instance, response);
                }, error -> {
                    // 处理错误
                    log.error("{} Pong Respond Error: {}", instance, error.getMessage());
                });
    }

    public Long durationSeconds() {
        Instant currentTime = Instant.now();
        Duration timeElapsed = Duration.between(lastRequestTime, currentTime);
        return timeElapsed.getSeconds();
    }

}
