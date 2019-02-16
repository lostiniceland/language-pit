package language.pit.debug;

import com.google.protobuf.InvalidProtocolBufferException;
import com.googlecode.protobuf.format.JsonFormat;
import common.infrastructure.protobuf.Events.EventsEnvelope;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaDebugListener {


	private static final Logger logger = LoggerFactory.getLogger(KafkaDebugListener.class);


	public static void main (String [] args){
		String kafkaHost = "localhost";
		String kafkaPort = "9094";
		String kafkaEventTopic = "language-pit.events";

		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost + ":" + kafkaPort);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "debug");
//		props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, "org.apache.kafka.clients.consumer.RoundRobinAssignor");
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

		KafkaConsumer<String, byte[]> kafkaConsumer = new KafkaConsumer<>(props);
		kafkaConsumer.subscribe(Collections.singletonList(kafkaEventTopic));
		while (!Thread.currentThread().isInterrupted()) {
			ConsumerRecords<String, byte[]> records;
			try {
				records = kafkaConsumer.poll(Duration.ofSeconds(20));

				for (ConsumerRecord<String, byte[]> record : records) {
					EventsEnvelope envelope;
					try {
						envelope = EventsEnvelope.parseFrom(record.value());
						new JsonFormat().printToString(envelope);
						System.out.println("============ Message received ============");
						System.out.println(envelope.toString());
						kafkaConsumer.commitSync();
					} catch (InvalidProtocolBufferException e) {
						logger.error("Error parsing event!", e);
					}
				}
			} catch (WakeupException e) {
				// ignore
			} catch (Exception e){
				logger.error("Error during poll", e);
			}
		}
		// commit offset until this point
		kafkaConsumer.close();
	}
}
