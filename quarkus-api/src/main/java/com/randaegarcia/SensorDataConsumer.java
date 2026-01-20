package com.randaegarcia;

import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class SensorDataConsumer {

  @Incoming("sensor_data")
  public void consume(JsonObject sensorJson) {
    SensorData data = sensorJson.mapTo(SensorData.class);
    System.out.println(data.id);
  }
}
