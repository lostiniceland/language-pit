package orchestration

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestProbe
import common.infrastructure.protobuf._
import org.scalatest.{Matchers, WordSpec}

class EventSubscriberTest extends WordSpec with Matchers with ScalatestRouteTest with EventSubscriber {

  val probe = TestProbe()

  override implicit val routerService: ActorRef = probe.ref

  "incoming requests" should {

    "match GET 'events/health' for availablity-checks" in {
      Get("/events/health") ~> route ~> check {
        status shouldEqual StatusCodes.OK
      }
    }

    // TODO atm Protobuff is able to unmarshall certain messages which are not typesaf (e.g. a BikeCreatedMessage can be unmarshalled into a BikeApprovedMessage)
    "match POST 'events/bikes/created' with BikeCreatedMessage in Protobuf format" in {
      val message = BikeCreatedMessage(bikeId = 1, value = 1).toByteArray
      Post("/events/bikes/created").withEntity(ProtobufSupport.headerContentTypeProto, message) ~> route ~> check {
        status shouldEqual StatusCodes.Accepted
      }
    }

    "match POST 'events/approvals/accepted' for BikeApprovedMessage in Protobuf format" in {
      val message = BikeApprovedMessage(bikeId = 1).toByteArray
      Post("/events/approvals/accepted").withEntity(ProtobufSupport.headerContentTypeProto, message) ~> route ~> check {
        status shouldEqual StatusCodes.Accepted
      }
    }

    "match POST 'events/approvals/rejected' for BikeRejectedMessage in Protobuf format" in {
      val message = BikeRejectedMessage(bikeId = 1).toByteArray
      Post("/events/approvals/accepted").withEntity(ProtobufSupport.headerContentTypeProto, message) ~> route ~> check {
        status shouldEqual StatusCodes.Accepted
      }
    }

  }
}
