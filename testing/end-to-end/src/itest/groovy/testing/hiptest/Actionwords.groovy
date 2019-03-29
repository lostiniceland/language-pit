package testing.hiptest

import groovyx.net.http.RESTClient
import testing.support.KafkaEventConsumer
import testing.support.StatefullActionword

import static groovyx.net.http.ContentType.JSON

class Actionwords extends StatefullActionword {

  Actionwords(KafkaEventConsumer eventConsumer) {
    super(eventConsumer)
  }

  /**
   * Adds a  new bike without any parts.
   */
	boolean iWantToBuyANewBikeWorthBikePrice(manufacturer = "Test-Manufacturer", name = "Test-Bike", weight = "13.5", value = "1000", bikePrice = "bikePrice") {
    def resp = bikeClient()
				.post(
        path: 'bikes',
        body: [
						manufacturer: manufacturer,
						name        : name,
						weight      : weight,
						value       : value
        ],
        requestContentType: JSON
		)
    assert resp.status == 201
    addState('created-bike-id', resp.headers['Location'].value.split('bikes/')[1])
    true
  }


  /**
   * Validates the status of a given bike according to the parameter ExpectedStatus
	 *
   * NOTE: makes use of shared state
   */
	boolean theBikeMustBeInTheCorrectStatus(expectedStatus = "Pending") {
    String bikeId = getState("created-bike-id", String)
    def resp = bikeClient()
        .get(
        path: 'bikes/' + bikeId,
        requestContentType: JSON
    )
    assert resp.status == 200
    assert resp.data.approval == expectedStatus.toUpperCase()
    true
  }


	boolean aNotificationAboutTheApprovalOutcomeMustBeSent(approvalResult = "") {
    String id = getState("created-bike-id", String.class)
    def event = eventConsumer.lookupEvent { envelope ->
			if (approvalResult == "accepted")
				return envelope.hasBikeApproved() && envelope.getBikeApproved().bikeId == Long.valueOf(id)
			else if (approvalResult == "rejected")
				return envelope.hasBikeRejected() && envelope.getBikeRejected().bikeId == Long.valueOf(id)
    }
    assert event.isPresent()
    true
  }

  /**
	 *
   * Perform a health-check against the bike-service to make sure it is up and running
   */
  boolean availabilityBikeService(){
    assert bikeClient().get(path: 'health').status == 200
    true
  }


	boolean cleanupState() {
    clearState()
    true
  }

  static RESTClient bikeClient(){
    return new RESTClient("http://${System.properties['SERVICE_HOST']}:${System.properties['BIKES_PORT']}/")
  }

  static RESTClient wifeClient(){
    return new RESTClient("http://${System.properties['SERVICE_HOST']}:${System.properties['WIFE_PORT']}/")
  }
}