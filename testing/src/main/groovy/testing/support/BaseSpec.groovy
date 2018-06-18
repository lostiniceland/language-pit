package testing.support

import spock.lang.Shared
import spock.lang.Specification

abstract class BaseSpec extends Specification {

  @Shared
  KafkaEventConsumer consumer

  def setupSpec(){
    System.properties.setProperty("KAFKA_HOST", "192.168.33.35")
    System.properties.setProperty("KAFKA_PORT", "9092")
    System.properties.setProperty("KAFKA_EVENT_TOPIC", "language-pit.events")
    consumer = new KafkaEventConsumer()
  }

}
