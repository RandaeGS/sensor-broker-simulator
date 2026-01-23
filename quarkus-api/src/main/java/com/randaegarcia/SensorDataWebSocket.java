package com.randaegarcia;

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import java.util.ArrayList;
import java.util.List;

@ServerEndpoint("/sensor-data")
@ApplicationScoped
public class SensorDataWebSocket {

    private static List<Session> sessions = new ArrayList<>();
    Logger logger = LoggerFactory.getLogger(SensorDataWebSocket.class);

    public static void broadcast(SensorData message) {
        sessions.forEach(s -> {
            s.getAsyncRemote().sendObject(Json.encode(message), result -> {
                if (result.getException() != null) {
                    System.out.println("Unable to send message: " + result.getException());
                }
            });
        });
    }

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        logger.info("Connected session: " + session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        logger.info("Disconnected session: " + session.getId());
    }
}
