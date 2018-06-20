package testing.support

import common.infrastructure.protobuf.Events
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory

class KafkaEventConsumer {

  static logger = LoggerFactory.getLogger(KafkaEventConsumer.class)

  private KafkaConsumer<String, byte[]> kafkaConsumer
  private kafkaHost = System.properties.getProperty("KAFKA_HOST")
  private kafkaPort = System.properties.getProperty("KAFKA_PORT")
  private topic = System.properties.getProperty("KAFKA_EVENT_TOPIC")

  KafkaEventConsumer(){
    Properties props = new Properties();
    props.put("bootstrap.servers", kafkaHost + ":" + kafkaPort)
    props.put("key.deserializer", StringDeserializer.class.getName())
    props.put("value.deserializer", ByteArrayDeserializer.class.getName())
    props.put("group.id", "testing")
    props.put("consumer.id", "testing-1")
    this.kafkaConsumer = new KafkaConsumer<>(props)
    kafkaConsumer.subscribe([topic])
 }

  boolean x(){
    ConsumerRecords<String, byte[]> records = kafkaConsumer.poll(500)
    def found = false
    records.forEach { record ->
      def envelope = Events.EventsEnvelope.parseFrom(record.value())
      if (envelope.hasBikeCreated()) {
        logger.info(envelope.getBikeCreated().toString())
        found = true
      }
    }
    found
  }
}
