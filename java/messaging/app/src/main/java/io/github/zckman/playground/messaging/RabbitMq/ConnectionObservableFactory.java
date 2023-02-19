package io.github.zckman.playground.messaging.RabbitMq;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.reactivex.rxjava3.core.Observable;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ConnectionObservableFactory {

    public static Observable<Connection> connection(ConnectionFactory factory, int pollIntervalMillis) {
        if (pollIntervalMillis < factory.getConnectionTimeout()) {
            throw new IllegalArgumentException("Polling faster than connection timeout is not allowed");
        }
        return Observable.interval(pollIntervalMillis, TimeUnit.MILLISECONDS)
                .map(tick -> getConnection(factory))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .take(1);
    }

    private static Optional<Connection> getConnection(ConnectionFactory factory) {
        try {
            return Optional.of(factory.newConnection());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return Optional.empty();
        }
    }
}
