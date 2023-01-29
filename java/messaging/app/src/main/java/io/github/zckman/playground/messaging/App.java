package io.github.zckman.playground.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvBuilder;
import io.github.zckman.playground.messaging.Kafka.KafkaServerObservableFactory;
import io.github.zckman.playground.messaging.SmartDevice.Sensor.Fake.FakeSensorFactory;
import io.github.zckman.playground.messaging.SmartDevice.SmartDevice;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import org.apache.kafka.clients.ClientDnsLookup;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class App {
    public static void main(String[] args) throws InterruptedException {
        // Load environment variables from .env file
        Dotenv dotenv = (new DotenvBuilder()).ignoreIfMissing().load();

        // Create some fake devices
        Observable<List<SmartDevice>> deviceListObservable = Observable.just(createDevices());
        Observable<SmartDevice> devicesObservable = deviceListObservable.flatMap(Observable::fromIterable);

        // Get the bootstrap servers and topic names
        final String bootstrapServers = dotenv.get("KAFKA_BOOTSTRAP_SERVERS");
        final String topicSensors = dotenv.get("TOPIC_SENSORS");
        final String topicSmartDevices = dotenv.get("TOPIC_SMART_DEVICES");

        Observable<KafkaProducer<String, String>> kafkaProducerSubject = createKafkaProducerObservable(bootstrapServers);

        kafkaProducerSubject.subscribe(kafkaProducer -> {
            // Subscribe producers to the devices
            ObjectMapper mapper = new ObjectMapper();
            deviceListObservable.map(devices -> {
                // Create JSON like [{id, [keys]}, ...]
                JsonNode node = mapper.valueToTree(
                        devices.stream().map(device -> Map.of(device.getId(), device.getSensorKeys())).toList()
                );
                return node;
            }).subscribe((JsonNode node) -> {
                String json = mapper.writeValueAsString(node);

                ProducerRecord<String, String> record = new ProducerRecord<>(topicSmartDevices, "devices", json);
                kafkaProducer.send(record);
            });

            // Flatten all Sensors and merge them
            Observable.merge(
                    devicesObservable.flatMap(smartDevice -> Observable.fromIterable(smartDevice.getSensors()))
            ).subscribe(sensorMeasurementTimed -> {
                SmartDevice.SensorMeasurement measurement = sensorMeasurementTimed.value();
                String key = measurement.getDeviceId() + "." + measurement.getKey();

                String json = mapper.writeValueAsString(
                        Map.of("timestamp", sensorMeasurementTimed.time(), "measurement", measurement)
                );

                ProducerRecord<String, String> record = new ProducerRecord<>(topicSensors, key, json);
                kafkaProducer.send(record);
            });
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

    public static Observable<KafkaProducer<String, String>> createKafkaProducerObservable(String bootstrapServers) {
        // Create a Properties for the KafkaProducer
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Completable completable = KafkaServerObservableFactory.waitForServers(bootstrapServers, ClientDnsLookup.USE_ALL_DNS_IPS, 10000, 500);

        return completable.andThen(Observable.fromCallable(()-> new KafkaProducer<String, String>(props)));
    }
}
