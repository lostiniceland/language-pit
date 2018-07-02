package testing.support

import spock.lang.Shared
import spock.lang.Specification

abstract class BaseSpec extends Specification {

  @Shared
  KafkaEventConsumer eventConsumer

  def setupSpec(){
    eventConsumer = new KafkaEventConsumer()
  }

  def cleanupSpec(){
    eventConsumer.close()
  }

}
