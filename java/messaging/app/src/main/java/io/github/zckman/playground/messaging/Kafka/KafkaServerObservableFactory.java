package io.github.zckman.playground.messaging.Kafka;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import org.apache.kafka.clients.ClientDnsLookup;
import org.apache.kafka.clients.ClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A class that provides an Observable that emits the address of a Kafka server as soon as it becomes available.
 */
public class KafkaServerObservableFactory {

    static Logger logger = LoggerFactory.getLogger(KafkaServerObservableFactory.class);

    /**
     *
     * @param bootstrapServers The bootstrap servers string in the format "host1:port1,host2:port2,..."
     * @param clientDnsLookup  The client DNS lookup policy
     * @param timeoutMillis    The timeout for the isReachable method in milliseconds
     * @param delay    The delay in milliseconds before isReachable is called
     * @return A Completable that completes when one bootstrap server is reachable
     */
    public static Completable waitForServers(String bootstrapServers, ClientDnsLookup clientDnsLookup, int timeoutMillis, long delay) {
        // Create a list of Completables that check if each server is online
        List<String> servers = Arrays.asList(bootstrapServers.split(","));
        List<InetSocketAddress> addresses = ClientUtils.parseAndValidateAddresses(servers, clientDnsLookup);
        List<Completable> completables = addresses.stream().map((address) -> {
            Completable completable = Completable.fromAction(() -> {
                logger.debug("Checking if {} is reachable", address);
                address.getAddress().isReachable(timeoutMillis);
                logger.info("{} is reachable", address);
            });
            return completable.delay(delay, TimeUnit.MILLISECONDS).retry();
        }).toList();

        // will complete if any items complete
        return Completable.amb(completables);
    }
}