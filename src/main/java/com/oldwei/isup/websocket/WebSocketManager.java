package com.oldwei.isup.websocket;

import java.util.concurrent.ConcurrentHashMap;

public class WebSocketManager {

    private static final ConcurrentHashMap<Integer, WebSocketServer> serverMap = new ConcurrentHashMap<>();

    public static synchronized WebSocketServer getOrCreateServer(int port) {
        if (serverMap.containsKey(port)) {
            return serverMap.get(port);
        }

        WebSocketServer server = new WebSocketServer(port);
        server.start();
        serverMap.put(port, server);
        return server;
    }

    public static void stopServer(int port) {
        WebSocketServer server = serverMap.remove(port);
        if (server != null) {
            try {
                server.stop();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void stopAll() {
        for (WebSocketServer server : serverMap.values()) {
            try {
                server.stop();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        serverMap.clear();
    }
}
