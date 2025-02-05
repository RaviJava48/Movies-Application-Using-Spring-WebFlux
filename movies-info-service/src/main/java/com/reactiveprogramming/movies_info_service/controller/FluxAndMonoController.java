package com.reactiveprogramming.movies_info_service.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.time.Duration;

@RestController
@RequestMapping("/api/v1")
public class FluxAndMonoController {

    @GetMapping("/flux")
    public Flux<Integer> integerFlux() {

        return Flux.just(1,2,3,4).log();
    }

    @GetMapping("/mono")
    public Mono<String> helloWorldMono() {

        return Mono.just("Hello World").log();
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Long> streamFlux() {

        return Flux.interval(Duration.ofSeconds(2)).log();
    }
}
