package io.github.zckman.playground.messaging.SmartDevice;

import io.github.zckman.playground.messaging.SmartDevice.Sensor.Reading;
import io.github.zckman.playground.messaging.SmartDevice.Sensor.Sensor;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Timed;

import java.util.*;

public class SmartDevice {

    public static class SensorReading {

        String deviceId;
        String key;
        Reading<Double> reading;

        public SensorReading(SmartDevice device, String key, Reading<Double> value) {
            this(device.getId(), key, value);
        }
        public SensorReading(String deviceId, String key, Reading<Double> value) {
            this.deviceId = deviceId;
            this.key = key;
            this.reading = value;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public String getKey() {
            return key;
        }

        public Reading<Double> getReading() {
            return reading;
        }
    }


    private final String id;
    private final Map<String, Sensor<Double>> sensors;

    /**
     *
     * @param id an unique device id
     * @param sensors all sensors for the device
     */
    public SmartDevice(String id, Map<String, Sensor<Double>> sensors) {
        this.id = id;
        this.sensors = sensors;
    }

    /**
     * Creates an {@link Observable} that transforms a {@link Reading} into a {@link SensorReading}
     * and wraps it with a timestamp
     * @param key a key like "temperature"
     * @param sensor a Sensor
     * @return the Observable
     */
    private Observable<Timed<SensorReading>> enrich(String key, Sensor<Double> sensor) {
        Observable<Reading<Double>> observable = Observable.wrap(sensor);
        return observable.map((Reading<Double> m) -> new SensorReading(this, key, m)).timestamp();
    };

    public Set<String> getSensorKeys(){
        return sensors.keySet();
    }

    /**
     * Adds a reference to this device and a key like temperature.
     * All values will be timestamped
     */
    public Observable<Timed<SensorReading>> getSensor(String key){
        if (!sensors.containsKey(key)) {
            throw new IllegalArgumentException("No such key: " + key);
        }
        return enrich(key, sensors.get(key));
    }

    public List<Observable<Timed<SensorReading>>> getSensors(){
        return this.sensors.entrySet().stream().map(
            (Map.Entry<String, Sensor<Double>> e) -> enrich(e.getKey(), e.getValue())
        ).toList();
    }

    public String getId() {
        return id;
    }
}
