package orchestration

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestProbe
import common.infrastructure.protobuf.events._
import org.scalatest.{Matchers, WordSpec}

class EventSubscriberTest extends WordSpec with Matchers with ScalatestRouteTest with EventSubscriber {

  val probe = TestProbe()

//  override implicit val routerService: ActorRef = probe.ref

  "incoming requests" should {

//    "match GET 'events/health' for availablity-checks" in {
//      Get("/events/health") ~> route ~> check {
//        status shouldEqual StatusCodes.OK
//      }
//    }

  }
}
