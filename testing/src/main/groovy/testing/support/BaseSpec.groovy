package testing.support

import spock.lang.Shared
import spock.lang.Specification

abstract class BaseSpec extends Specification {

  @Shared
  KafkaEventConsumer consumer

  def setupSpec(){
    consumer = new KafkaEventConsumer()
  }

}
