package io.github.zckman.playground.messaging.SmartDevice.Sensor;

import io.reactivex.rxjava3.core.ObservableSource;

public interface Sensor<T extends Number> extends ObservableSource<Measurement<T>> {

    Measurement<T> getLastMeasurement();
}
