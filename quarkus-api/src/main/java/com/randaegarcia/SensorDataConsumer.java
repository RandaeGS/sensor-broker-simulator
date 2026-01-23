package com.randaegarcia;

import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

@ApplicationScoped
public class SensorDataConsumer {

    @Incoming("sensor_data")
    @Outgoing("sensor")
    public void consume(JsonObject sensorJson) {
        SensorData data = sensorJson.mapTo(SensorData.class);
        SensorDataWebSocket.broadcast(data);
    }
}
