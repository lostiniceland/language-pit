package orchestration.camunda;

import com.google.protobuf.InvalidProtocolBufferException;
import common.infrastructure.protobuf.Events.BikeApprovedMessage;
import common.infrastructure.protobuf.Events.BikeCreatedMessage;
import common.infrastructure.protobuf.Events.BikeRejectedMessage;
import common.infrastructure.protobuf.Events.EventsEnvelope;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.concurrent.ManagedTask;
import javax.enterprise.concurrent.ManagedTaskListener;
import javax.inject.Inject;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaConsumerTask implements Runnable, ManagedTask {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private KafkaConsumer<String, byte[]> kafkaConsumer;

  @Resource(lookup = "kafkaHost")
  private String kafkaHost;
  @Resource(lookup = "kafkaPort")
  private String kafkaPort;
  @Resource(lookup = "kafkaEventTopic")
  private String kafkaEventTopic;
  @Inject
  private ProcessEngine engine;


  @Override
  public void run() {
    Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost + ":" + kafkaPort);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "orchestration");
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

    this.kafkaConsumer = new KafkaConsumer<>(props);
    kafkaConsumer.subscribe(Collections.singletonList(kafkaEventTopic));
    while (!Thread.currentThread().isInterrupted()) {
      ConsumerRecords<String, byte[]> records;
      try {
        records = kafkaConsumer.poll(Duration.ofSeconds(1));

        for (ConsumerRecord<String, byte[]> record : records) {
          EventsEnvelope envelope;
          try {
            envelope = EventsEnvelope.parseFrom(record.value());

            if (envelope.hasBikeCreated()){
              startProcess(envelope.getBikeCreated());
            }else if (envelope.hasBikeApproved()){
              startProcess(envelope.getBikeApproved());
            }else if (envelope.hasBikeRejected()){
              startProcess(envelope.getBikeRejected());
            }

          } catch (InvalidProtocolBufferException e) {
            logger.error("Error parsing event!", e);
          }
        }
      } catch (WakeupException e) {
        // ignore
      }
    }
    // commit offset until this point
    kafkaConsumer.commitSync(); // FIXME should only commit offsets for completed events
    kafkaConsumer.close();
  }


  @Override
  public ManagedTaskListener getManagedTaskListener() {
    // handle task-execution
    return new ManagedTaskListener() {
      @Override
      public void taskSubmitted(Future<?> future, ManagedExecutorService executor, Object task) {
        logger.info("Scheduled Kafka-Task in thread {}", Thread.currentThread().getName());
      }

      @Override
      public void taskAborted(Future<?> future, ManagedExecutorService executor, Object task, Throwable exception) {
        logger.info("Kafka-Task aborted in thread {}", Thread.currentThread().getName());
      }

      @Override
      public void taskDone(Future<?> future, ManagedExecutorService executor, Object task, Throwable exception) {
        logger.info("Kafka-Task completed in thread {}", Thread.currentThread().getName());
      }

      @Override
      public void taskStarting(Future<?> future, ManagedExecutorService executor, Object task) {
        logger.info("Starting Kafka-Task in thread {}", Thread.currentThread().getName());
      }
    };
  }

  @Override
  public Map<String, String> getExecutionProperties() {
    return Collections.emptyMap();
  }


  // FIXME use Proto-Messages instead of simple-types
  private void startProcess(BikeCreatedMessage message){
    Map<String, Object> variables = new HashMap<>(3);
    variables.put("type", "bikeCreated");
    variables.put("bikeId", message.getBikeId());
    variables.put("value", message.getValue());
    invokeProcess("EventReceived", variables);
  }

  private void startProcess(BikeApprovedMessage message){
    Map<String, Object> variables = new HashMap<>(3);
    variables.put("type", "approvalAccepted");
    variables.put("bikeId", message.getBikeId());
    invokeProcess("EventReceived", variables);
  }

  private void startProcess(BikeRejectedMessage message){
    Map<String, Object> variables = new HashMap<>(3);
    variables.put("type", "approvalRejected");
    variables.put("bikeId", message.getBikeId());
    invokeProcess("EventReceived", variables);
  }

  private ProcessInstance invokeProcess(String messageName, Map<String, Object> variables){
    ProcessInstance instance = null;
    try{
      instance = engine.getRuntimeService().startProcessInstanceByMessage(messageName, variables);
      logger.info("Process with ID '{}' started", instance.getId());
    }catch(ProcessEngineException e){
      logger.error("Error invoking Process-Instance", e);
    }
    return instance;
  }
}
