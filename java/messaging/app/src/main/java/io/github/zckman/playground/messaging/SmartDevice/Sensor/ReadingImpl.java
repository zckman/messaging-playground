package io.github.zckman.playground.messaging.SmartDevice.Sensor;

public class ReadingImpl<T extends Number> implements Reading<T> {
    T value;
    String unit;

    public ReadingImpl(T value, String unit) {
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
