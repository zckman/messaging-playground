package io.github.zckman.playground.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvBuilder;
import io.github.zckman.playground.messaging.SmartDevice.Sensor.Fake.FakeSensorFactory;
import io.github.zckman.playground.messaging.SmartDevice.SmartDevice;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Timed;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class App {

    // Load environment variables from .env file
    static Dotenv dotenv = (new DotenvBuilder()).ignoreIfMissing().load();

    public static void main(String[] args) throws InterruptedException {

        // Create some fake devices
        Observable<List<SmartDevice>> deviceListObservable = Observable.just(createDevices());
        Observable<SmartDevice> devicesObservable = deviceListObservable.flatMap(Observable::fromIterable);

        // Get topic names
        final String topicSensors = dotenv.get("TOPIC_SENSORS");
        final String topicSmartDevices = dotenv.get("TOPIC_SMART_DEVICES");

        // Map devices to JSON
        ObjectMapper mapper = new ObjectMapper();
        Observable<JsonNode> devicesJson = deviceListObservable.map(devices -> {
            // Create JSON like [{id, [keys]}, ...]
            JsonNode node = mapper.valueToTree(
                    devices.stream().map(device -> Map.of(device.getId(), device.getSensorKeys())).toList()
            );
            return node;
        });
        // Flatten all sensors and merge them
        Observable<Timed<SmartDevice.SensorReading>> readings = Observable.merge(
                devicesObservable.flatMap(smartDevice -> Observable.fromIterable(smartDevice.getSensors()))
        );

        });

        keepRunning();
    }

    public static List<SmartDevice> createDevices() {
        SmartDevice kitchen = new SmartDevice(
                "kitchen.climate", Map.of(
                "temperature", FakeSensorFactory.createTemperatureSensor(5, TimeUnit.SECONDS, 0.1, 18, 25),
                "relative humidity", FakeSensorFactory.createRelativeHumiditySensor(5, TimeUnit.SECONDS, 1, 40, 80),
                "air pressure", FakeSensorFactory.createAirPressureSensor(5, TimeUnit.SECONDS, 1, 1000, 1025)
        )
        );
        SmartDevice bedroom = new SmartDevice(
                "bedroom.climate", Map.of(
                "temperature", FakeSensorFactory.createTemperatureSensor(5, TimeUnit.SECONDS, 0.1, 18, 25),
                "relative humidity", FakeSensorFactory.createRelativeHumiditySensor(5, TimeUnit.SECONDS, 1, 40, 80),
                "air pressure", FakeSensorFactory.createAirPressureSensor(5, TimeUnit.SECONDS, 1, 1000, 1025)
        )
        );
        SmartDevice outside = new SmartDevice(
                "outside.climate", Map.of(
                "temperature", FakeSensorFactory.createTemperatureSensor(5, TimeUnit.SECONDS, 0.1, 4, 16),
                "relative humidity", FakeSensorFactory.createRelativeHumiditySensor(5, TimeUnit.SECONDS, 1, 40, 80),
                "air pressure", FakeSensorFactory.createAirPressureSensor(5, TimeUnit.SECONDS, 1, 1000, 1025)
        )
        );

        return Arrays.asList(kitchen, bedroom, outside);
    }

    public static void keepRunning() throws InterruptedException {
        Thread thread = new Thread(() -> {
            while (true) {

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        thread.join();
    }



    }
}
