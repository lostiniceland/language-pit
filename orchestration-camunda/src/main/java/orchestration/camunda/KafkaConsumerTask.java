package orchestration.camunda;

import com.google.protobuf.InvalidProtocolBufferException;
import common.infrastructure.protobuf.Events.EventsEnvelope;
import java.time.Duration;
import java.util.Collections;
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
import org.apache.kafka.clients.consumer.RoundRobinAssignor;
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
    props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, "org.apache.kafka.clients.consumer.RoundRobinAssignor");
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"); // TODO use confirmed offset

    this.kafkaConsumer = new KafkaConsumer<>(props);
    kafkaConsumer.subscribe(Collections.singletonList(kafkaEventTopic));
    while (!Thread.currentThread().isInterrupted()) {
      ConsumerRecords<String, byte[]> records;
      try {
        records = kafkaConsumer.poll(Duration.ofSeconds(5));

        for (ConsumerRecord<String, byte[]> record : records) {
          EventsEnvelope envelope;
          try {
            envelope = EventsEnvelope.parseFrom(record.value());
            invokeProcess("EventReceived", Collections.singletonMap("envelope", envelope));
            kafkaConsumer.commitSync();
          } catch (InvalidProtocolBufferException e) {
            logger.error("Error parsing event!", e);
          }
        }
      } catch (WakeupException e) {
        // ignore
      } catch (Exception e){
        // we catch everythin in order to continue...for now TODO imporove error handling
        logger.error("Error during poll", e);
      }
    }
    // commit offset until this point
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
        if (exception != null){
          logger.error("Kafka-Task aborted with error in thread {}", Thread.currentThread().getName(), exception);
        } else {
          logger.info("Kafka-Task aborted in thread {}", Thread.currentThread().getName());
        }
      }

      @Override
      public void taskDone(Future<?> future, ManagedExecutorService executor, Object task, Throwable exception) {
        if (exception != null){
          logger.error("Kafka-Task completed with error in thread {}", Thread.currentThread().getName(), exception);
        } else {
          logger.info("Kafka-Task completed in thread {}", Thread.currentThread().getName());
        }
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


  private ProcessInstance invokeProcess(String messageName, Map<String, Object> variables){
    ProcessInstance instance = null;
    try{
      instance = engine.getRuntimeService().startProcessInstanceByMessage(messageName, variables);
      if(instance.isEnded()){
        logger.info("Process with ID '{}' finished.", instance.getId());
      }else{
        logger.warn("Process with ID '{}' did not finish!", instance.getId());
      }
    }catch(ProcessEngineException e){
      logger.error("Error invoking Process-Instance", e);
    }
    return instance;
  }
}
