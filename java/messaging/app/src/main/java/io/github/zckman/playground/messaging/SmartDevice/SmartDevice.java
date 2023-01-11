package io.github.zckman.playground.messaging.SmartDevice;

import io.github.zckman.playground.messaging.SmartDevice.Sensor.Measurement;
import io.github.zckman.playground.messaging.SmartDevice.Sensor.Sensor;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Timed;

import java.util.*;

public class SmartDevice {

    public class SensorMeasurement<T extends Number> {

        SmartDevice device;
        String key;
        Measurement<T> measurement;

        public SensorMeasurement(SmartDevice device, String key, Measurement<T> value) {
            this.device = device;
            this.key = key;
            this.measurement = value;
        }

        public Measurement<T> getMeasurement() {
            return measurement;
        }
    }


    private String id;
    private Map<String, Sensor<? extends Number>> sensors;

    /**
     *
     * @param id an unique device id
     * @param sensors all sensors for the device
     */
    public SmartDevice(String id, Map<String, Sensor<? extends Number>> sensors) {
        this.id = id;
        this.sensors = sensors;
    }

    /**
     * Creates an {@link Observable} that transforms a {@link Measurement} into a {@link SensorMeasurement}
     * and wraps it with a timestamp
     * @param key a key like "temperature"
     * @param sensor a Sensor
     * @return the Observable
     */
    private Observable<Timed<SensorMeasurement<Number>>> enrich(String key, Sensor<Number> sensor) {
        Observable<Measurement<Number>> observable = Observable.wrap(sensor);
        return observable.map((Measurement<Number> m) -> new SensorMeasurement<>(this, key, m)).timestamp();
    };

    public Set<String> getSensorKeys(){
        return sensors.keySet();
    }

    /**
     * Adds a reference to this device and a key like temperature.
     * All values will be timestamped
     */
    public Observable<Timed<SensorMeasurement<Number>>> getSensor(String key){
        if (!sensors.containsKey(key)) {
            throw new IllegalArgumentException("No such key: " + key);
        }
        return enrich(key, sensors.get(key));
    }

    public List<Observable<Timed<SensorMeasurement<Number>>>> getSensors(){
        return this.sensors.entrySet().stream().map(
            (Map.Entry<String, Sensor<Number>> e) -> enrich(e.getKey(), e.getValue())
        ).toList();
    }

    public String getId() {
        return id;
    }
}
