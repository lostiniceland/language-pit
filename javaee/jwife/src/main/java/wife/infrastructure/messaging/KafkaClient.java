package wife.infrastructure.messaging;

import com.google.protobuf.Timestamp;
import common.infrastructure.protobuf.Events.BikeApprovalCreatedMessage;
import common.infrastructure.protobuf.Events.BikeApprovedMessage;
import common.infrastructure.protobuf.Events.BikeRejectedMessage;
import common.infrastructure.protobuf.Events.EventsEnvelope;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wife.application.ApplicationRuntimeException;
import wife.application.ExternalEventPublisher;
import wife.domain.BikeApprovalCreatedEvent;
import wife.domain.BikeApprovedEvent;
import wife.domain.BikeRejectedEvent;

@ApplicationScoped
public class KafkaClient implements ExternalEventPublisher {

  private static final Logger logger = LoggerFactory.getLogger(KafkaClient.class);

  @Resource(lookup = "kafkaHost")
  private String kafkaHost;
  @Resource(lookup = "kafkaPort")
  private String kafkaPort;
  @Resource(lookup = "kafkaEventTopic")
  private String kafkaEventTopic;

  private KafkaProducer<String, byte[]> kafkaProducer;


  @PostConstruct
  protected void init() {
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost + ":" + kafkaPort);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
    props.put(ProducerConfig.CLIENT_ID_CONFIG, "wife");
    props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, "2000");
    props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, "4000");
    this.kafkaProducer = new KafkaProducer<>(props);
  }


  @Override
  public void notifyAboutApprovalCreated(BikeApprovalCreatedEvent event) {
    logger.info("Sending event '{}' to Kafka", event.toString());
    EventsEnvelope envelope = EventsEnvelope.newBuilder()
        .setOccuredOn(Timestamp.newBuilder()
            .setSeconds(event.getOccuredOn().toInstant().getEpochSecond())
            .setNanos(event.getOccuredOn().toInstant().getNano()))
        .setBikeApprovalCreated(
            BikeApprovalCreatedMessage.newBuilder()
                .setApprovalId(event.getId())
                .setBikeId(event.getBikeId())
                .build()).build();
    send(envelope);
  }

  @Override
  public void notifyAboutApproval(BikeApprovedEvent event) {
    logger.info("Sending event '{}' to Kafka", event.toString());
    EventsEnvelope envelope = EventsEnvelope.newBuilder()
        .setOccuredOn(Timestamp.newBuilder()
            .setSeconds(event.getOccuredOn().toInstant().getEpochSecond())
            .setNanos(event.getOccuredOn().toInstant().getNano()))
        .setBikeApproved(
            BikeApprovedMessage.newBuilder()
                .setApprovalId(event.getId())
                .setBikeId(event.getBikeId())
                .build()).build();
    send(envelope);
  }

  @Override
  public void notifyAboutReject(BikeRejectedEvent event) {
    logger.info("Sending event '{}' to Kafka", event.toString());
    EventsEnvelope envelope = EventsEnvelope.newBuilder()
        .setOccuredOn(Timestamp.newBuilder()
            .setSeconds(event.getOccuredOn().toInstant().getEpochSecond())
            .setNanos(event.getOccuredOn().toInstant().getNano()))
        .setBikeRejected(
            BikeRejectedMessage.newBuilder()
                .setApprovalId(event.getId())
                .setBikeId(event.getBikeId())
                .build()).build();
    send(envelope);
  }

  /**
   * Executes a blocking call to the KafkaProducer
   * @param envelope the message to send
   * @throws ApplicationRuntimeException when the message could not be send (whatever the cause)
   */
  private void send(EventsEnvelope envelope) {
    try {
      Future<RecordMetadata> future = kafkaProducer.send(
          new ProducerRecord<>(kafkaEventTopic, envelope.toByteArray()));
      future.get(); // Block until delivered
    } catch (KafkaException | InterruptedException | ExecutionException e) {
      logger.error("Could not send event {} to Kafka", e);
      throw new ApplicationRuntimeException("Communication with Kafka failed");
    }
  }
}
