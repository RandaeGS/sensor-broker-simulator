package com.randaegarcia;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

import java.time.LocalDate;

@Entity
public class SensorData extends PanacheEntity {
    public Integer id;
    public Double temperature;
    public Double humidity;
    public LocalDate generationDate;
}
