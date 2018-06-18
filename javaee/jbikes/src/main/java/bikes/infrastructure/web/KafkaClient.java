package bikes.infrastructure.web;

import bikes.application.ExternalEventPublisher;
import bikes.domain.BikeCreatedEvent;
import common.infrastructure.protobuf.Events.BikeCreatedMessage;
import common.infrastructure.protobuf.Events.EventsEnvelope;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    props.put("bootstrap.servers", kafkaHost + ":" + kafkaPort);
    props.put("key.serializer", StringSerializer.class.getName());
    props.put("value.serializer", ByteArraySerializer.class.getName());
    props.put("client.id", "bikes-1");
    this.kafkaProducer = new KafkaProducer<>(props);
  }

  @Override
  public void notifyWifeAboutNewBike(BikeCreatedEvent event) {
    EventsEnvelope envelope = EventsEnvelope.newBuilder().setBikeCreated(
        BikeCreatedMessage.newBuilder()
          .setBikeId(event.getBikeId())
          .setValue(event.getValue())
          .build()).build();
    kafkaProducer.send(new ProducerRecord<>(kafkaEventTopic, envelope.toByteArray()));
    kafkaProducer.flush();
  }
}
