package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.FileOutputStream;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.time.Duration;
import java.time.Instant;
import java.io.File;

/**
 * @author: chenhs
 * @date: Created in 19:17 2024/9/25
 **/
@Slf4j
@Service
public class PingService {
    private static final String LOCK_FILE_PATH = "ping.lock";
    private static int requestCount = 0;
    private static Instant lastRequestTime = Instant.now();

    @Autowired
    RestTemplate restTemplate;

    public String callPongService(String say) {
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
                log.info("Request sent: {}", say);
                requestCount++;
                //res = "pong req";
                res = restTemplate.getForObject("http://pong-service/pong?say=" + say, String.class);
                log.info("Pong Respond: {}", res);
            } else {
                log.info("Request not send as being \"rate limited\"");
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


    public String callPongServiceNoLock(String say) {
        log.info("Request sent: {}", say);
        String res = restTemplate.getForObject("http://pong-service/pong?say=" + say, String.class);
        log.info("Pong Respond: {}", res);
        return res;
    }
}
