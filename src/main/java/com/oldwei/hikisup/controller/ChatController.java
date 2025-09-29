package com.oldwei.hikisup.controller;

import com.oldwei.hikisup.handler.ConnectionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

    private final ConnectionManager connectionManager;

    @GetMapping("/connections/count")
    public Mono<Integer> getConnectionCount() {
        return Mono.just(connectionManager.getConnectionCount());
    }

    @GetMapping(value = "/connections/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Integer> streamConnectionCount() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(tick -> connectionManager.getConnectionCount())
                .distinctUntilChanged()
                .mergeWith(connectionManager.getConnectionCountUpdates());
    }

    @GetMapping("/connections/users")
    public Mono<List<String>> getOnlineUsers() {
        return Mono.just(connectionManager.getOnlineUsers());
    }

    @GetMapping("/chat")
    public Mono<org.springframework.core.io.Resource> getChatPage() {
        return Mono.fromSupplier(() -> new ClassPathResource("static/chat.html"));
    }
}