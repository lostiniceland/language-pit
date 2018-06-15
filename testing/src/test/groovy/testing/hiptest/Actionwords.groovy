package testing.hiptest

import groovyx.net.http.RESTClient
import static groovyx.net.http.ContentType.*

class Actionwords {

  /**
   * Adds a  new bike without any parts.
   */
  boolean createBike(manufacturer = "Test-Manufacturer", name = "Test-Bike", weight = "13.5", value = "1000"){
    def resp = bikeClient()
      .post(
        path: 'bikes',
        body: [
          manufacturer: manufacturer,
          name: name,
          weight: weight,
          value: value
        ],
        requestContentType: JSON
      )
    assert resp.status == 201
    true
  }


  /**
   * Validates the status of a given bike according to the parameter ExpectedStatus
   */
  boolean checkApprovalstatusOfBike(expectedStatus = "Pending"){

    true
  }


  boolean checkApprovalstatusWithWife(expectedStatus = "Pending"){

    true
  }


  /**
   * 
   * Perform a health-check against the bike-service to make sure it is up and running
   */
  boolean availabilityBikeService(){
    assert bikeClient().get(path: 'bikes/health').status == 200
    true
  }


  /**
   * Perform a health-check against the wife-service to make sure it is up and running
   */
  boolean availabilityWifeService(){
    assert wifeClient().get(path: 'wife/health').status == 200
    true
  }


  /**
   * Perform a health-check against the orchestration-layer to make sure it is up and running
   */
  boolean availabilityOrchestrationLayer(){
    assert new RESTClient("http://localhost:9090/events/health").get([:]).status == 200
    true
  }


  static RESTClient bikeClient(){
    return new RESTClient("http://localhost:8080/")
  }

  static RESTClient wifeClient(){
    return new RESTClient("http://localhost:8090/")
  }

}