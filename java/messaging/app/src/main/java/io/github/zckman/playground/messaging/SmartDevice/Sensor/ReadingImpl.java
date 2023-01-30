package io.github.zckman.playground.messaging.SmartDevice.Sensor;

public class MeasurementImpl<T extends Number> implements Measurement<T> {
    T value;
    String unit;

    public MeasurementImpl(T value, String unit) {
        this.value = value;
        this.unit = unit;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public String getUnit() {
        return unit;
    }
}
