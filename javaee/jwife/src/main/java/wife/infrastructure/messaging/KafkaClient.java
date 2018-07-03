package wife.infrastructure.messaging;

import com.google.protobuf.Timestamp;
import common.infrastructure.protobuf.Events.BikeApprovalCreatedMessage;
import common.infrastructure.protobuf.Events.BikeApprovedMessage;
import common.infrastructure.protobuf.Events.BikeRejectedMessage;
import common.infrastructure.protobuf.Events.EventsEnvelope;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wife.application.ExternalEventPublisher;
import wife.domain.BikeApprovalCreatedEvent;
import wife.domain.BikeApprovedEvent;
import wife.domain.BikeRejectedEvent;

@Kafka
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
  protected void init(){
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost + ":" + kafkaPort);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
    props.put(ProducerConfig.CLIENT_ID_CONFIG, "wife");
    this.kafkaProducer = new KafkaProducer<>(props);
  }


  @Override
  public void notifyAboutApprovalCreated(BikeApprovalCreatedEvent event) {
    EventsEnvelope envelope = EventsEnvelope.newBuilder()
        .setOccuredOn(Timestamp.newBuilder()
            .setSeconds(event.getOccuredOn().toInstant().getEpochSecond())
            .setNanos(event.getOccuredOn().toInstant().getNano()))
        .setBikeApprovalCreated(
            BikeApprovalCreatedMessage.newBuilder()
                .setApprovalId(event.getId())
                .setBikeId(event.getBikeId())
                .build()).build();
    kafkaProducer.send(new ProducerRecord<>(kafkaEventTopic, envelope.toByteArray()));
    kafkaProducer.flush();
  }

  @Override
  public void notifyAboutApproval(BikeApprovedEvent event) {
    EventsEnvelope envelope = EventsEnvelope.newBuilder()
        .setOccuredOn(Timestamp.newBuilder()
            .setSeconds(event.getOccuredOn().toInstant().getEpochSecond())
            .setNanos(event.getOccuredOn().toInstant().getNano()))
        .setBikeApproved(
          BikeApprovedMessage.newBuilder()
            .setApprovalId(event.getId())
            .setBikeId(event.getBikeId())
            .build()).build();
    kafkaProducer.send(new ProducerRecord<>(kafkaEventTopic, envelope.toByteArray()));
    kafkaProducer.flush();
  }

  @Override
  public void notifyAboutReject(BikeRejectedEvent event) {
    EventsEnvelope envelope = EventsEnvelope.newBuilder()
        .setOccuredOn(Timestamp.newBuilder()
            .setSeconds(event.getOccuredOn().toInstant().getEpochSecond())
            .setNanos(event.getOccuredOn().toInstant().getNano()))
        .setBikeRejected(
            BikeRejectedMessage.newBuilder()
                .setApprovalId(event.getId())
                .setBikeId(event.getBikeId())
                .build()).build();
    kafkaProducer.send(new ProducerRecord<>(kafkaEventTopic, envelope.toByteArray()));
    kafkaProducer.flush();
  }
}
