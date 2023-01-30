package io.github.zckman.playground.messaging.SmartDevice.Sensor;

public interface Reading<T extends Number> {

    T getValue();

    String getUnit();
}
