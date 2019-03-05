package bikes.domain

import domain.bikes.ApprovalStatus
import domain.bikes.Bike
import domain.bikes.Part
import nl.jqno.equalsverifier.EqualsVerifier
import spock.lang.Specification
import spock.lang.Unroll

class DomainSpec extends Specification {

  Bike sut

  def setup(){
    sut = new Bike("manufacturer", "name", 12.0F, 4000F)
  }

  def 'Part is a ValueObject' () {
    given:
    Part partA = new Part("name", 2f)
    Part partB = new Part("name", 2f)
    expect:
    partA == partB
    and:
    EqualsVerifier.forClass(Part).usingGetClass().verify()
  }


  def 'only Approved bikes can be updated'() {
    given:
    sut.@approval = ApprovalStatus.Accepted
    when:
    sut.update(
        'changedManufacturer',
        'changedName',
        11F,
        5000F,
        [new Part('newPart', 2.0F)])
    then:
    sut.name == 'changedName'
  }

  @Unroll ("#approval bikes cannot be updated")
  def 'not approved bikes cannot be updated'() {
    given:
    sut.@approval = approval
    when:
    sut.update(
        'changedManufacturer',
        'changedName',
        11.0F,
        5000F,
        [new Part('newPart', 2.0F)])
    then:
      thrown IllegalStateException
    where:
    approval << [ApprovalStatus.Pending, ApprovalStatus.Rejected]
  }

}
