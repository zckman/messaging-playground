package io.github.zckman.playground.messaging.Kafka;

import io.reactivex.rxjava3.core.Observable;
import org.apache.kafka.clients.ClientDnsLookup;
import org.apache.kafka.clients.ClientUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A class that provides an Observable that emits the address of a Kafka server as soon as it becomes available.
 */
public class KafkaServerObservableFactory {

    /**
     * Creates an Observable that emits the address of a Kafka server as soon as it becomes available.
     *
     * @param bootstrapServers   The bootstrap servers string in the format "host1:port1,host2:port2,..."
     * @param clientDnsLookup    The client DNS lookup policy
     * @param pollIntervalMillis The poll interval in milliseconds
     * @return An Observable that emits the address of a Kafka server as soon as it becomes available
     */
    public static Observable<InetSocketAddress> create(String bootstrapServers, ClientDnsLookup clientDnsLookup, int pollIntervalMillis) {
        return Observable.interval(pollIntervalMillis, TimeUnit.MILLISECONDS)
                .map(tick -> getKafkaServerAddress(bootstrapServers, clientDnsLookup, pollIntervalMillis))
                .filter(address -> address != null)
                .take(1);
    }

    /**
     * Returns the first Kafka server's address that is online, or null if none of the servers are online.
     *
     * @param bootstrapServers The bootstrap servers string in the format "host1:port1,host2:port2,..."
     * @param clientDnsLookup  The client DNS lookup policy
     * @param timeoutMillis    The timeout for the isReachable method in milliseconds
     * @return The first Kafka server's address that is online, or null if none of the servers are online
     */
    private static InetSocketAddress getKafkaServerAddress(String bootstrapServers, ClientDnsLookup clientDnsLookup, int timeoutMillis) {
        // Create a list of observables that check if each server is online
//        List<Observable<String>> observables = new ArrayList<>();
        List<String> servers = Arrays.asList(bootstrapServers.split(","));
        List<InetSocketAddress> addresses = ClientUtils.parseAndValidateAddresses(servers, clientDnsLookup);
        List<Observable<InetSocketAddress>> observables = addresses.stream().map((address) -> {
                    Observable<InetSocketAddress> observable = Observable.fromCallable(() -> {
                        try {
                            if (address.getAddress().isReachable(timeoutMillis)) {
                                return address;
                            } else {
                                return null;
                            }
                        } catch (IOException e) {
                            return null;
                        }
                    });
            return observable;
        }).toList();

        // Merge the observables and take the first non-null value
        return Observable.merge(observables)
                .firstElement()
                .blockingGet();
    }
}