package com.randaegarcia;

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/sensor-data/{sensor}")
@ApplicationScoped
public class SensorDataWebSocket {

    private static final Map<Session, Integer> sessions = new ConcurrentHashMap<>();
    Logger logger = LoggerFactory.getLogger(SensorDataWebSocket.class);

    public static void broadcast(SensorData message) {
        sessions.entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(message.id))
                .forEach(entry -> entry.getKey().getAsyncRemote().sendObject(Json.encode(message)));
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("sensor") Integer sensor) {
        sessions.put(session, sensor);
        logger.info("Connected session: " + session.getId() + " for sensor: " + sensor);
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        logger.info("Disconnected session: " + session.getId());
    }
}
