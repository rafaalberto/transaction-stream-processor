package io.rafaalberto.transactionstreamprocessor.integration.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

public final class KafkaTestConsumer<T> implements AutoCloseable {

  private final KafkaConsumer<String, byte[]> consumer;
  private final ObjectMapper objectMapper;
  private final Class<T> valueType;

  public KafkaTestConsumer(
      final String bootstrapServers, final String topic, final Class<T> valueType) {

    this.valueType = valueType;

    Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group-" + System.nanoTime());
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);

    this.consumer = new KafkaConsumer<>(props);
    this.consumer.subscribe(List.of(topic));

    this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
  }

  public List<T> poll(final Duration timeout) {
    ConsumerRecords<String, byte[]> records = consumer.poll(timeout);

    List<T> events = new ArrayList<>();

    for (ConsumerRecord<String, byte[]> consumerRecord : records) {
      events.add(deserialize(consumerRecord.value()));
    }

    return events;
  }

  private T deserialize(final byte[] payload) {
    try {
      return objectMapper.readValue(payload, valueType);
    } catch (Exception e) {
      throw new RuntimeException("Failed to deserialize Kafka message", e);
    }
  }

  @Override
  public void close() {
    consumer.close();
  }
}
