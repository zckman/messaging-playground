package io.github.zckman.playground.messaging.kafka;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class KafkaObserver implements Observer<Object> {

  private final String topic;
  private final String bootstrapServers;
  private final Producer<String, Object> producer;

  public KafkaObserver(String topic) {
    this.topic = topic;
    this.bootstrapServers = "localhost:" + System.getenv("KAFKA_PORT");

    Properties properties = new Properties();
    properties.put("bootstrap.servers", bootstrapServers);
    properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    this.producer = new KafkaProducer<>(properties);
  }

  @Override
  public void onSubscribe(Disposable d) {
  }

  @Override
  public void onNext(Object o) {
    producer.send(new ProducerRecord<>(topic, o.toString()));
  }

  @Override
  public void onError(Throwable e) {
  }

  @Override
  public void onComplete() {
  }
}
