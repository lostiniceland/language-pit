package orchestration

object DefaultMessages {

  sealed trait DefaultMessage

  case class Start() extends DefaultMessage
  case class Stop() extends DefaultMessage
  case class Continue() extends DefaultMessage
  case class WakeUp() extends DefaultMessage
  case class StatusRequest() extends DefaultMessage

}
