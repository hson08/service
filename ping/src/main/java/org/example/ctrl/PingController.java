package org.example.ctrl;

import lombok.extern.slf4j.Slf4j;
import org.example.service.PingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author: chenhs
 * @date: Created in 4:13 2024/9/26
 **/
@Slf4j
@RestController
public class PingController {

    @Autowired
    PingService pingService;

    @GetMapping(value = "/ping", produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> getPing(@RequestParam(name = "instance") String instance, @RequestParam(name = "say") String say) {
        String res = pingService.callPongService(instance, say);
        if (res == null){
            return Mono.just("pong service no result, please check the request parameters.");
        } else {
            return Mono.just(res);
        }
    }



}
