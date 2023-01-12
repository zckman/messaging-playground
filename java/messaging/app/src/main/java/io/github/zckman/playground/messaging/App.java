package io.github.zckman.playground.messaging;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvBuilder;
import io.github.zckman.playground.messaging.Kafka.KafkaServerObservableFactory;
import io.github.zckman.playground.messaging.SmartDevice.Sensor.Fake.FakeSensorFactory;
import io.github.zckman.playground.messaging.SmartDevice.SmartDevice;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.ReplaySubject;
import org.apache.kafka.clients.ClientDnsLookup;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.net.InetSocketAddress;
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

        // Get the bootstrap servers
        final String bootstrapServers = dotenv.get("KAFKA_BOOTSTRAP_SERVERS");

        // Create an Observable that emits the address of a Kafka server as soon as it becomes available
        // We use this Observable to wait until one server is up but will use the original bootstrapServers string later
        Observable<InetSocketAddress> serverObservable = KafkaServerObservableFactory.create(bootstrapServers, ClientDnsLookup.USE_ALL_DNS_IPS, 1000);

        // Create a ReplaySubject to always get the latest servers on subscribe
        ReplaySubject<InetSocketAddress> availableServer = ReplaySubject.createWithSize(1);
        serverObservable.subscribe(availableServer);

        // Subscribe to the Observable and print the server address when it becomes available
        availableServer.subscribe(serverAddress -> System.out.println("Kafka server is online: " + serverAddress));

        // Create and emit a KafkaProducer when Servers are available
        // TODO: Decide on actual message type
        ReplaySubject<KafkaProducer<String, String>> kafkaProducerSubject = ReplaySubject.createWithSize(1);

        availableServer.map(serverAddress -> {
            // Create a KafkaProducer with the server address
            Properties props = new Properties();
            // We add the servers not just the first reachable
            props.put("bootstrap.servers", bootstrapServers);
            props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            return new KafkaProducer<String, String>(props);
        }).subscribe(kafkaProducerSubject);

        kafkaProducerSubject.subscribe(kafkaProducer -> {
            // Do something with the KafkaProducer
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
