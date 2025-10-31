package com.oldwei.isup.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.oldwei.isup.domain.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ChatWebSocketHandler implements WebSocketHandler {

    private final ConnectionManager connectionManager;
    private final ObjectMapper objectMapper;

    public ChatWebSocketHandler(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private final Map<String, Sinks.Many<ChatMessage>> sessionSinks = new ConcurrentHashMap<>();
    private final Sinks.Many<ChatMessage> globalMessageSink = Sinks.many().multicast().onBackpressureBuffer();

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String sessionId = session.getId();
        String username = "User-" + (sessionId.length() >= 8 ? sessionId.substring(0, 8) : sessionId);

        log.info("WebSocket connection established: {} for user: {}", sessionId, username);

        connectionManager.addConnection(sessionId, username);

        Sinks.Many<ChatMessage> messageSink = Sinks.many().multicast().onBackpressureBuffer();
        sessionSinks.put(sessionId, messageSink);

        ChatMessage joinMessage = ChatMessage.builder()
                .type(ChatMessage.MessageType.JOIN)
                .sender("System")
                .content(username + " joined the chat")
                .timestamp(LocalDateTime.now())
                .build();

        globalMessageSink.tryEmitNext(joinMessage);

        Mono<Void> input = session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .doOnNext(messageText -> {
                    try {
                        ChatMessage message = objectMapper.readValue(messageText, ChatMessage.class);
                        message.setSender(username);
                        message.setTimestamp(LocalDateTime.now());
                        message.setType(ChatMessage.MessageType.CHAT);

                        globalMessageSink.tryEmitNext(message);
                        log.debug("Received message from {}: {}", username, message.getContent());
                    } catch (Exception e) {
                        log.error("Error parsing message from {}: {}", username, messageText, e);
                    }
                })
                .doOnError(error -> log.error("Error in WebSocket receive for {}: {}", username, error.getMessage()))
                .doOnCancel(() -> handleDisconnection(sessionId, username))
                .doOnTerminate(() -> handleDisconnection(sessionId, username))
                .then();

        Flux<WebSocketMessage> output = Flux.merge(
                        messageSink.asFlux().map(msg -> {
                            try {
                                return objectMapper.writeValueAsString(msg);
                            } catch (Exception e) {
                                log.error("Error serializing message", e);
                                return "";
                            }
                        }),
                        globalMessageSink.asFlux()
                                .filter(msg -> {
                                    // 对于CHAT消息，只发送给其他用户（不发送给发送者）
                                    // 对于JOIN/LEAVE消息，发送给所有用户
                                    return msg.getType() != ChatMessage.MessageType.CHAT || !msg.getSender().equals(username);
                                })
                                .map(msg -> {
                                    try {
                                        return objectMapper.writeValueAsString(msg);
                                    } catch (Exception e) {
                                        log.error("Error serializing message", e);
                                        return "";
                                    }
                                })
                )
                .filter(json -> !json.isEmpty())
                .map(session::textMessage)
                .doOnError(error -> log.error("Error in WebSocket send for {}: {}", username, error.getMessage()));

        return Mono.zip(input, session.send(output)).then();
    }

    private void handleDisconnection(String sessionId, String username) {
        log.info("WebSocket connection closed: {} for user: {}", sessionId, username);

        connectionManager.removeConnection(sessionId);
        sessionSinks.remove(sessionId);

        ChatMessage leaveMessage = ChatMessage.builder()
                .type(ChatMessage.MessageType.LEAVE)
                .sender("System")
                .content(username + " left the chat")
                .timestamp(LocalDateTime.now())
                .build();

        globalMessageSink.tryEmitNext(leaveMessage);
    }
}