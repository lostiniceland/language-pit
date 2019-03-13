package infrastructure.camunda

import org.camunda.bpm.dmn.engine.DmnDecisionResult
import org.camunda.bpm.engine.test.Deployment
import org.camunda.bpm.engine.test.ProcessEngineRule
import org.junit.Rule
import spock.lang.Specification
import spock.lang.Unroll

import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.processEngine

class WifeDecisionSpec extends Specification {

	@Rule
	public ProcessEngineRule processEngineRule = new ProcessEngineRule()

	@Deployment(resources = ["wife_bike.dmn"])
	@Unroll
	def "Decision evaluates to #resultMessage for price=#bikePrice, alreadyOwned=#bikesOwned"() {
		given:
		Map<String, Object> vars = ["bikePrice": bikePrice, "bikesOwned": bikesOwned]

		when:
		DmnDecisionResult result = processEngine().getDecisionService()
				.evaluateDecisionByKey("Decision_Bike_Rating")
				.variables(vars)
				.evaluate()


		then:
		result.getSingleResult().get("approved") == approvalResult
		result.getSingleResult().get("manualApproval") == manualApprovalResult

		where:
		resultMessage            | bikePrice | bikesOwned | approvalResult | manualApprovalResult
		"automatically approved" | 1000      | 0          | true           | false
		"automatically approved" | 1999      | 6          | false          | true
		"manual approval"        | 2000      | 5          | false          | true
		"automatically approved" | 2000      | 3          | false          | true
		"manual approval"        | 5000      | 4          | false          | true
		"automatically declined" | 5000      | 5          | false          | false
		"manual approval"        | 10000     | 0          | false          | true
		"automatically declined" | 10000     | 3          | false          | false
	}

}