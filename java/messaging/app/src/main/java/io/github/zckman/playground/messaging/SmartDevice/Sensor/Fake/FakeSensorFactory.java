package io.github.zckman.playground.messaging.SmartDevice.Sensor.Fake;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.github.zckman.playground.messaging.SmartDevice.Sensor.MeasurementImpl;
import io.github.zckman.playground.messaging.SmartDevice.Sensor.Sensor;
import io.github.zckman.playground.messaging.SmartDevice.Sensor.SensorSubject;
import io.reactivex.rxjava3.core.Observable;

public class FakeSensorFactory {
    public static Observable<Double> create(long interval, TimeUnit timeUnit, double factor, double minimumValue, double maximumValue) {
        return Observable.interval(interval, timeUnit).map(tick -> new Random().nextGaussian() * factor).scan((accumulator, value) -> {
            double result = accumulator + value;
            // Keep the value between the minimum and maximum values
            result = Math.max(minimumValue, result);
            result = Math.min(maximumValue, result);
            return result;
        });
    }

    public static Sensor<Double> createSensor(long interval, TimeUnit timeUnit, double factor, double minimumValue, double maximumValue, String unit) {
        Observable<MeasurementImpl<Double>> measurement = create(interval, timeUnit, factor, minimumValue, maximumValue).map((Double d) -> new MeasurementImpl<>(d, unit));
        SensorSubject<Double> sensor = new SensorSubject<>();
        measurement.subscribe(sensor);
        return sensor;
    }

    public static Sensor<Double> createTemperatureSensor(long interval, TimeUnit timeUnit, double factor, double minimumValue, double maximumValue) {
        return createSensor(interval, timeUnit, factor, minimumValue, maximumValue, "°C");
    }

    public static Sensor<Double> createRelativeHumiditySensor(long interval, TimeUnit timeUnit, double factor, double minimumValue, double maximumValue) {
        return createSensor(interval, timeUnit, factor, minimumValue, maximumValue, "%");
    }

    public static Sensor<Double> createAirPressureSensor(long interval, TimeUnit timeUnit, double factor, double minimumValue, double maximumValue) {
        return createSensor(interval, timeUnit, factor, minimumValue, maximumValue, "mbar");
    }
}
