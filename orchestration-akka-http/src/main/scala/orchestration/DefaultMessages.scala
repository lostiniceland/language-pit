package orchestration

import scala.concurrent.duration.{Duration, FiniteDuration}

object DefaultMessages {

  sealed trait DefaultMessage

  case class Start() extends DefaultMessage
  case class Stop() extends DefaultMessage
  case class Continue() extends DefaultMessage
  case class Sleep(duration: FiniteDuration) extends DefaultMessage
  case class WakeUp() extends DefaultMessage

}
