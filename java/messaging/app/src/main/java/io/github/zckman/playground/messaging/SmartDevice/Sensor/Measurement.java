package io.github.zckman.playground.messaging.SmartDevice.Sensor;

public interface Measurement<T extends Number> {

    T getValue();

    String getUnit();
}
