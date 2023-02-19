package io.github.zckman.playground.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvBuilder;
import io.github.zckman.playground.messaging.RabbitMq.ConnectionObservableFactory;
import io.github.zckman.playground.messaging.SmartDevice.Sensor.Fake.FakeSensorFactory;
import io.github.zckman.playground.messaging.SmartDevice.SmartDevice;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Timed;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class App {

    public static final String DEVICES_QUEUE = "devices_queue";
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

        Observable<Channel> channelObservable = getRabbitMqChannelObservable();

        channelObservable.subscribe(channel -> {

            AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                    .contentType("application/json")
                    .contentEncoding(StandardCharsets.UTF_8.name())
                    .build();

            //TODO: we don't need two exchanges since we can have multiple queues


            channel.queueDeclare(DEVICES_QUEUE, false, false, false, null);
            channel.queueBind(DEVICES_QUEUE, topicSmartDevices, "devices");
            createLoggingObserver(channel, DEVICES_QUEUE);

            devicesJson
                    .map(mapper::writeValueAsString)
                    .subscribe(json -> {
                        channel.basicPublish(
                                topicSmartDevices,
                                "devices",
                                properties,
                                json.getBytes(StandardCharsets.UTF_8)
                        );
                    });

            //create queues for device readings and bind to routing keys
            devicesObservable.subscribe(device -> {
                String queue = getQueueForId(device.getId());
                String routing = getRoutingPrefixForId(device.getId()) + "#";
                channel.queueDeclare(queue, false, false, false, null);
                channel.queueBind(queue, topicSensors, routing);

                //log messages
                createLoggingObserver(channel, queue);
                //TODO: handle changes in devices, remove subscriptions etc
            });

            // Publish incoming readings
            readings.subscribe(sensorReadingTimed -> {
                SmartDevice.SensorReading reading = sensorReadingTimed.value();
                String routing = getRoutingPrefixForId(reading.getDeviceId()) + reading.getKey();

                String json = mapper.writeValueAsString(
                        Map.of("timestamp", sensorReadingTimed.time(), "reading", reading)
                );

                channel.basicPublish(
                        topicSensors,
                        routing,
                        properties,
                        json.getBytes(StandardCharsets.UTF_8)
                );
            });
        });

        keepRunning();
    }

    private static String getQueueForId(String id) {
        return "device_" + id;
    }

    private static String getRoutingPrefixForId(String id) {
        return "device." + id + ".";
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

    public static Observable<Channel> getRabbitMqChannelObservable() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(dotenv.get("RABBITMQ_AMQP_HOST"));
        factory.setPort(Integer.parseInt(dotenv.get("RABBITMQ_AMQP_PORT")));
        factory.setUsername(dotenv.get("RABBITMQ_USER"));
        factory.setPassword(dotenv.get("RABBITMQ_PASS"));
        factory.setConnectionTimeout(1000);

        Observable<Connection> connectionObservable = ConnectionObservableFactory.connection(factory, 1000);

        return connectionObservable.map((Connection c) -> c.createChannel());
    }

    /**
     * Creates an Observer that logs messages to System.out
     */
    private static void createLoggingObserver(Channel channel, String queue) {
        Observable.create(emitter ->
                channel.basicConsume(queue, true, (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    emitter.onNext(
                            String.format("%s:%s:%s",
                                    delivery.getEnvelope().getExchange(),
                                    delivery.getEnvelope().getRoutingKey(),
                                    message
                            ));
                }, consumerTag -> {
                })).subscribe(System.out::println);
    }
}
