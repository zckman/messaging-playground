package io.github.zckman.playground.messaging;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.zckman.playground.messaging.Kafka.KafkaServerObservableFactory;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.ReplaySubject;
import org.apache.kafka.clients.ClientDnsLookup;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.net.InetSocketAddress;
import java.util.Properties;

public class App {
    public static void main(String[] args) {
        // Load environment variables from .env file
        // TODO: ensure .env is in the current working directory
        // TODO: maybe allow setting an alternate path
        Dotenv dotenv = Dotenv.load();

        // Get the bootstrap servers
        String bootstrapServers = dotenv.get("KAFKA_BOOTSTRAP_SERVERS");

        // Create an Observable that emits the address of a Kafka server as soon as it becomes available
        Observable<InetSocketAddress> serverObservable = KafkaServerObservableFactory.create(bootstrapServers, ClientDnsLookup.USE_ALL_DNS_IPS, 1000);
        // Create a ReplaySubject to always get the latest server on subscribe
        ReplaySubject<InetSocketAddress> availableServer = ReplaySubject.createWithSize(1);
        serverObservable.subscribe(availableServer);

        // Subscribe to the Observable and print the server address when it becomes available
        availableServer.subscribe(serverAddress -> System.out.println("Kafka server is online: " + serverAddress.getHostString()));

        // Create and emit a KafkaProducer when Servers are available
        // TODO: Decide on actual message type
        ReplaySubject<KafkaProducer<String, String>> kafkaProducerSubject = ReplaySubject.createWithSize(1);

        availableServer.map(serverAddress -> {
            // Create a KafkaProducer with the server address
            Properties props = new Properties();
            // We could add all the servers here not just the first reachable
            props.put("bootstrap.servers", serverAddress.getHostString());
            // TODO: Add additional properties here?
            return new KafkaProducer<String, String>(props);
        }).subscribe(kafkaProducerSubject);

        kafkaProducerSubject.subscribe(kafkaProducer -> {
            // Do something with the KafkaProducer
        });

    }
}
