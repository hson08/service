package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;


@Slf4j
@Service
public class PingService {
    public static final String LOCK_FILE_1  = "lock1.lock";
    public static final String LOCK_FILE_2  = "lock2.lock";
    File lockFile1 = new File(LOCK_FILE_1);
    File lockFile2 = new File(LOCK_FILE_2);

    public String callPongService(String instance, String say) {
        String res = "";

        try (FileOutputStream stream1 = new FileOutputStream(lockFile1);
             FileOutputStream stream2 = new FileOutputStream(lockFile2);
             FileChannel channel1 = stream1.getChannel();
             FileChannel channel2 = stream2.getChannel()) {

            FileLock lock1 = null;
            FileLock lock2 = null;

            try {
                // 尝试获取第一个锁
                lock1 = channel1.tryLock();

                if (lock1 != null) {
                    call(instance, say);// 处理请求
                } else {
                    // 尝试获取第二个锁
                    lock2 = channel2.tryLock();

                    if (lock2 != null) {
                        call(instance, say);// 处理请求
                    } else {
                        log.info("{} Request not send as being \"rate limited\"", instance);
                    }
                }
            } finally {
                // 释放锁
                if (lock1 != null) {
                    lock1.release();
                    log.info("{} Released lock 1.", instance);
                }
                if (lock2 != null) {
                    lock2.release();
                    log.info("{} Released lock 2.", instance);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }

    public void call(String instance, String say) {
        try {
            Thread.sleep(500L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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



}
