package testing.support;

import com.google.protobuf.InvalidProtocolBufferException;
import common.infrastructure.protobuf.Events.EventsEnvelope;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Predicate;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaEventConsumer {

  static Logger logger = LoggerFactory.getLogger(KafkaEventConsumer.class);

  private final KafkaConsumer<String, byte[]> kafkaConsumer;

  KafkaEventConsumer() {
    Properties defaultConfig = new Properties();
    try {
      defaultConfig.load(getClass().getClassLoader().getResourceAsStream("testing.properties"));
    } catch (IOException e) {
      logger.error("Could not load properties for testing", e);
    }
    String kafkaHostOverride = System.getProperties().getProperty("KAFKA_HOST");
    String kafkaPortOverride = System.getProperties().getProperty("KAFKA_PORT");
    String kafkaTopicOverride = System.getProperties().getProperty("KAFKA_EVENT_TOPIC");
    final String kafkaHost = kafkaHostOverride != null ? kafkaHostOverride : defaultConfig.getProperty("KAFKA_HOST");
    final String kafkaPort = kafkaPortOverride != null ? kafkaPortOverride : defaultConfig.getProperty("KAFKA_PORT");
    final String topic = kafkaTopicOverride != null ? kafkaTopicOverride : defaultConfig.getProperty("KAFKA_EVENT_TOPIC");


    Properties props = new Properties();
    props.put("bootstrap.servers", kafkaHost + ":" + kafkaPort);
    props.put("key.deserializer", StringDeserializer.class.getName());
    props.put("value.deserializer", ByteArrayDeserializer.class.getName());
    props.put("group.id", "testing");
    this.kafkaConsumer = new KafkaConsumer<>(props);
    kafkaConsumer.subscribe(Collections.singletonList(topic));
  }

  public Optional<EventsEnvelope> lookupEvent(Predicate<EventsEnvelope> predicate) {
    ConsumerRecords<String, byte[]> records = kafkaConsumer.poll(500);
    try {
      for (ConsumerRecord<String, byte[]> record : records) {
        EventsEnvelope envelope = EventsEnvelope.parseFrom(record.value());
        if (predicate.test(envelope)) {
          return Optional.of(envelope);
        }
      }
    } catch (InvalidProtocolBufferException e) {
      logger.error(e.getMessage());
    } finally {

    }
    return Optional.empty();
  }

  public void close() {
    kafkaConsumer.close();
  }
}
