package com.coin_notify.coin_notify.controller;

import com.coin_notify.coin_notify.scheduler.PriceScheduler;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.UUID;

@RestController
public class SseController {
    public final Sinks.Many<ServerSentEvent<String>> sink;
    private final PriceScheduler priceScheduler;

    public SseController(PriceScheduler priceScheduler) {
        this.priceScheduler = priceScheduler;
        this.sink = Sinks.many().multicast().onBackpressureBuffer();
    }

    @GetMapping("/sse/{uuid}")
    public Flux<ServerSentEvent<String>> subscribe(@PathVariable UUID uuid) {
        Sinks.Many<ServerSentEvent<String>> sink = Sinks.many().unicast().onBackpressureBuffer();
        priceScheduler.getUserSinkMap().put(uuid, sink);
        return sink.asFlux()
                .doOnCancel(() -> {
                    priceScheduler.getUserSinkMap().remove(uuid);
                });
    }
}

