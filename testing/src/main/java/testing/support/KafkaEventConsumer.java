package testing.support;

import com.google.protobuf.InvalidProtocolBufferException;
import common.infrastructure.protobuf.Events.EventsEnvelope;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class launches a KafkaConsumer-instance in a dedicated thread which polls for new messages.
 * Available events/messages can be looked up.
 */
public class KafkaEventConsumer {

  static Logger logger = LoggerFactory.getLogger(KafkaEventConsumer.class);

  private final Collection<EventsEnvelope> events = new ArrayList<>();

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

    final ConsumerLoop consumer = new ConsumerLoop(kafkaHost, kafkaPort, topic);
    final ExecutorService executorService = Executors.newSingleThreadExecutor();
    executorService.submit(consumer);
    // Make sure to stop the thread and cleanup resources when the JVM get stopped
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      consumer.shutdown();
      executorService.shutdown();
      try{
        executorService.awaitTermination(1, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        logger.error(e.getMessage(), e);
      }
    }));
  }


  /**
   * Checks against all, until now, received {@link EventsEnvelope}s from the topic and tests each with the given predicate.
   * If the predicate did not match the loop continues until the default timeout of 3 seconds.
   * @param predicate the test-function applied against each new {@link EventsEnvelope}
   * @return the first envelope wrapped in an Optional that passed the predicate, otherwise empty.
   */
  public Optional<EventsEnvelope> lookupEvent(Predicate<EventsEnvelope> predicate) {
    return lookupEvent(predicate, 3000);
  }

  /**
   * Checks against all, until now, received {@link EventsEnvelope}s from the topic and tests each with the given predicate.
   * If the predicate did not match the loop continues until the given timeout is reached.
   * @param predicate the test-function applied against each new {@link EventsEnvelope}
   * @param timeout custom timeout until the method will timeout its retries
   * @return the first envelope wrapped in an Optional that passed the predicate, otherwise empty.
   */
  public Optional<EventsEnvelope> lookupEvent(Predicate<EventsEnvelope> predicate, long timeout) {
    long deadline = System.currentTimeMillis() + timeout;

    while(System.currentTimeMillis() < deadline) {
      Optional<EventsEnvelope> result;
      synchronized (events) {
        result = events.stream().filter(predicate).findFirst();
      }
      if(result.isPresent()){
        return result;
      }
    }
    return Optional.empty();
  }

  private final class ConsumerLoop implements Runnable {

    private final KafkaConsumer<String, byte[]> kafkaConsumer;

    private ConsumerLoop(String kafkaHost, String kafkaPort, String topic){
      Properties props = new Properties();
      props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost + ":" + kafkaPort);
      props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
      props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
      props.put(ConsumerConfig.GROUP_ID_CONFIG, "testing");
      this.kafkaConsumer = new KafkaConsumer<>(props);

      // Use manual partition-assignment to not fall into rebalancing-issues
      List<TopicPartition> partitions = new ArrayList<>();
      for (PartitionInfo partition : kafkaConsumer.partitionsFor(topic))
        partitions.add(new TopicPartition(topic, partition.partition()));
      kafkaConsumer.assign(partitions);
    }

    @Override
    public void run() {
      try{
        while(true){
          // polling will wait indefinately until an event is available or shut down
          ConsumerRecords<String, byte[]> records = kafkaConsumer.poll(Long.MAX_VALUE);
          for (ConsumerRecord<String, byte[]> record : records) {
            EventsEnvelope envelope = EventsEnvelope.parseFrom(record.value());
            synchronized (events){
              events.add(envelope);
            }
          }
        }
      } catch (InvalidProtocolBufferException e) {
        logger.error(e.getMessage());
      } catch(WakeupException e) {
        // ignore for shutdown
      }finally {
        kafkaConsumer.close();
      }
    }

    private void shutdown(){
      // cause a WakeupException
      kafkaConsumer.wakeup();
    }
  }
}
