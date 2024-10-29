package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.time.Duration;
import java.time.Instant;


@Slf4j
@Service
public class PingService {
    public static final String LOCK_FILE_PATH = "ping.lock";
    public static int requestCount = 0;
    public static Instant lastRequestTime = Instant.now();


    public String callPongService(String instance, String say) {
        File lockFile = new File(LOCK_FILE_PATH);
        String res = "";
        try (FileOutputStream fos = new FileOutputStream(lockFile)) {
            FileLock lock = fos.getChannel().lock();

            Instant currentTime = Instant.now();
            Duration timeElapsed = Duration.between(lastRequestTime, currentTime);

            if (timeElapsed.getSeconds() >= 1) {
                requestCount = 0;
                lastRequestTime = currentTime;
            }

            if (requestCount < 2) {
                log.info("{} Request sent: {}", instance, say);
                requestCount++;

                //res = restTemplate.getForObject("http://pong-service/pong?say=" + say, String.class);
                log.info("Pong Respond: {}", res);

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
            } else {
                log.info("{} Request not send as being \"rate limited\"", instance);
            }
            lock.release();
        } catch (OverlappingFileLockException e) {
            log.error("Caught OverlappingFileLockException: {}", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    }



}
