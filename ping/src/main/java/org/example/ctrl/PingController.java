package org.example.ctrl;

import lombok.RequiredArgsConstructor;
import org.example.service.PingService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.File;


@RestController
@RequiredArgsConstructor
public class PingController {

    private final PingService pingService;

    @GetMapping(value = "/ping", produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> getPing(@RequestParam(name = "instance") String instance, @RequestParam(name = "say") String say) {
        String res = pingService.callPongService(instance, say, true);
        return Mono.just(res);
    }


}
