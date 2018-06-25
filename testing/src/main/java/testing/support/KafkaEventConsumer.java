package testing.support;

import com.google.protobuf.InvalidProtocolBufferException;
import common.infrastructure.protobuf.Events.EventsEnvelope;
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

  private KafkaConsumer<String, byte[]> kafkaConsumer;
  private String kafkaHost = System.getProperties().getProperty("KAFKA_HOST");
  private String kafkaPort = System.getProperties().getProperty("KAFKA_PORT");
  private String topic = System.getProperties().getProperty("KAFKA_EVENT_TOPIC");

  KafkaEventConsumer() {
    Properties props = new Properties();
    props.put("bootstrap.servers", kafkaHost + ":" + kafkaPort);
    props.put("key.deserializer", StringDeserializer.class.getName());
    props.put("value.deserializer", ByteArrayDeserializer.class.getName());
    props.put("group.id", "testing");
//    props.put("consumer.id", "testing-1");
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
//      kafkaConsumer.commitSync();
    }
    return Optional.empty();
  }
}
