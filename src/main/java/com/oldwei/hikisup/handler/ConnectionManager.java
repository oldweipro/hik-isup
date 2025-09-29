package com.oldwei.hikisup.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ConnectionManager {

    private final Map<String, String> activeConnections = new ConcurrentHashMap<>();
    private final Sinks.Many<Integer> connectionCountSink = Sinks.many().multicast().onBackpressureBuffer();

    public void addConnection(String sessionId, String username) {
        activeConnections.put(sessionId, username);
        broadcastConnectionCount();
        log.info("Added connection for user: {} (session: {})", username, sessionId);
    }

    public void removeConnection(String sessionId) {
        String username = activeConnections.remove(sessionId);
        if (username != null) {
            broadcastConnectionCount();
            log.info("Removed connection for user: {} (session: {})", username, sessionId);
        }
    }

    public int getConnectionCount() {
        return activeConnections.size();
    }

    public Flux<Integer> getConnectionCountUpdates() {
        return connectionCountSink.asFlux();
    }

    private void broadcastConnectionCount() {
        int count = getConnectionCount();
        connectionCountSink.tryEmitNext(count);
    }

    public String getUsername(String sessionId) {
        return activeConnections.get(sessionId);
    }

    public List<String> getOnlineUsers() {
        return new ArrayList<>(activeConnections.values());
    }
}