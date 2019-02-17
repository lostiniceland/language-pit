package pit.performance.gatling

import bikes.infrastructure.protobuf.bikes.{CreateBikeMessage, PartType}
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import scalapb.json4s.JsonFormat

import scala.language.postfixOps


class LoadSimulation extends Simulation {

	private val httpConfig = http.baseUrl("http://localhost:8080/")


	val addBike: ChainBuilder = exec(http("create new bike")
			.post("bikes")
			.header(HttpHeaderNames.ContentType, HttpHeaderValues.ApplicationJson)
			.body(
				StringBody(
					JsonFormat.toJsonString(
						CreateBikeMessage("Manufacturer", "Name", 16.0f, 1000.0f, Seq(PartType("PartA", 2.0f), PartType("PartB", 1.5f)))
					)
				)
			)
			.check(headerRegex(HttpHeaderNames.Location, """(?<=http://localhost:8080/bikes/)\d+""").saveAs("bikeId"))
	)

	val checkApproval: ChainBuilder = exec(http("check approval for bike-id ${bikeId}")
			.get("bikes/${bikeId}")
			.check(status.is(200))
	)

	private val scn = scenario("Add Bike")
			.exec(addBike)
			.pause(1)
			tryMax(5){
				exec(checkApproval)
			}


	setUp(scn.inject(atOnceUsers(100))).protocols(httpConfig)

}
