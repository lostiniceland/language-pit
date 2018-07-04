package testing.support;

import com.google.protobuf.InvalidProtocolBufferException;
import common.infrastructure.protobuf.Events.EventsEnvelope;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Predicate;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
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
      defaultConfig.load(getClass().getResourceAsStream("/testing.properties"));
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
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost + ":" + kafkaPort);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
    props.put(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG, 10000);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "testing");
    this.kafkaConsumer = new KafkaConsumer<>(props);

    // Use manual partition-assignment to not fall into rebalancing-issues
    List<TopicPartition> partitions = new ArrayList<>();
    for (PartitionInfo partition : kafkaConsumer.partitionsFor(topic))
      partitions.add(new TopicPartition(topic, partition.partition()));
    kafkaConsumer.assign(partitions);
    // do an intial poll for proper connection-setup TODO improve this
    kafkaConsumer.poll(0);
  }

  /**
   * Retrieves all {@link EventsEnvelope}s from the topic and tests each with the given predicate
   * @param predicate the test-function applied against each new {@link EventsEnvelope}
   * @return the first envelope wrapped in an Optional that passed the predicate, otherwise empty.
   */
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
    }
    return Optional.empty();
  }

  public void close() {
    if(kafkaConsumer != null)
      kafkaConsumer.close();
  }
}
