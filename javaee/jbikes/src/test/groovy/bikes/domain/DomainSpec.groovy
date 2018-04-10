package bikes.domain

import spock.lang.Specification
import spock.lang.Unroll

class DomainSpec extends Specification {

  def Bike sut

  def setup(){
    sut = new Bike("manufacturer", "name", 12.0F, 4000F)
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
