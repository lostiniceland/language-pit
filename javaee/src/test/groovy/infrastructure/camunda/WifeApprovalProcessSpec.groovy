package infrastructure.camunda

import org.camunda.bpm.engine.delegate.JavaDelegate
import org.camunda.bpm.engine.test.Deployment
import org.camunda.bpm.engine.test.ProcessEngineRule
import org.camunda.bpm.engine.test.mock.Mocks
import org.junit.Rule
import spock.lang.Specification

import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.assertThat
import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.processEngine
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.complete
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables

class WifeApprovalProcessSpec extends Specification {

	@Rule
	public ProcessEngineRule processEngineRule = new ProcessEngineRule()

	def setup() {
		Mocks.register("wifeCreateBikeApproval", Mock(JavaDelegate))
		Mocks.register("wifeAcceptBike", Mock(JavaDelegate))
		Mocks.register("wifeRejectBike", Mock(JavaDelegate))
	}

	@Deployment(resources = ["wife_bike.bpmn", "wife_bike.dmn"])
	def "Process with automatic approval"() {
		given:
		def vars = [bikePrice: bikePrice, bikesOwned: bikesOwned]

		when:
		def instance = processEngineRule.getRuntimeService().startProcessInstanceByMessage(WifeCamundaBpmnProcess.MESSAGE, WifeCamundaBpmnProcess.BUSINESS_KEY_BIKE_APPROVAL, vars)

		then:
		assertThat(instance).hasPassed("Task_CreateApproval")
		assertThat(instance).hasPassed("Task_Decide_Bike_Automatically")

		and: 'bike has been approved'
		assertThat(instance).hasPassed("Task_Approve_Bike")

		and: 'process is ended'
		assertThat(instance).isEnded()

		where:
		bikePrice | bikesOwned
		1000      | 1
	}

	@Deployment(resources = ["wife_bike.bpmn", "wife_bike.dmn"])
	def "Process with automatic rejection"() {
		given:
		def vars = [bikePrice: bikePrice, bikesOwned: bikesOwned]

		when:
		def instance = processEngineRule.getRuntimeService().startProcessInstanceByMessage(WifeCamundaBpmnProcess.MESSAGE, WifeCamundaBpmnProcess.BUSINESS_KEY_BIKE_APPROVAL, vars)

		then:
		assertThat(instance).hasPassed("Task_CreateApproval")
		assertThat(instance).hasPassed("Task_Decide_Bike_Automatically")

		and: 'bike has been rejected'
		assertThat(instance).hasPassed("Task_Reject_Bike")

		and: 'process is ended'
		assertThat(instance).isEnded()

		where:
		bikePrice | bikesOwned
		10000     | 2
	}


	@Deployment(resources = ["wife_bike.bpmn", "wife_bike.dmn"])
	def "Process with manual approval"() {
		given:
		def vars = [bikePrice: bikePrice, bikesOwned: bikesOwned]

		when:
		def instance = processEngineRule.getRuntimeService().startProcessInstanceByMessage(WifeCamundaBpmnProcess.MESSAGE, WifeCamundaBpmnProcess.BUSINESS_KEY_BIKE_APPROVAL, vars)

		then:
		assertThat(instance).hasPassed("Task_CreateApproval")
		assertThat(instance).hasPassed("Task_Decide_Bike_Automatically")

		and: 'bike needs manual approval'
		assertThat(instance).isWaitingAt("Task_Decide_Bike_Manually")

		when: 'manual approval'
		def task = processEngine().getTaskService().createTaskQuery().taskDefinitionKey("Task_Decide_Bike_Manually").singleResult()
		complete(task, withVariables("manualDecision", true))

		then: 'bike gets approved'
		assertThat(instance).hasPassed("Task_Approve_Bike")

		and: 'process is ended'
		assertThat(instance).isEnded()

		where:
		bikePrice | bikesOwned
		5000      | 3
	}


	@Deployment(resources = ["wife_bike.bpmn", "wife_bike.dmn"])
	def "Process with manual reject"() {
		given:
		def vars = [bikePrice: bikePrice, bikesOwned: bikesOwned]

		when:
		def instance = processEngineRule.getRuntimeService().startProcessInstanceByMessage(WifeCamundaBpmnProcess.MESSAGE, WifeCamundaBpmnProcess.BUSINESS_KEY_BIKE_APPROVAL, vars)

		then:
		assertThat(instance).hasPassed("Task_CreateApproval")
		assertThat(instance).hasPassed("Task_Decide_Bike_Automatically")

		and: 'bike needs manual approval'
		assertThat(instance).isWaitingAt("Task_Decide_Bike_Manually")

		when: 'manual approval'
		def task = processEngine().getTaskService().createTaskQuery().taskDefinitionKey("Task_Decide_Bike_Manually").singleResult()
		complete(task, withVariables("manualDecision", false))

		then: 'bike gets rejected'
		assertThat(instance).hasPassed("Task_Reject_Bike")

		and: 'process is ended'
		assertThat(instance).isEnded()

		where:
		bikePrice | bikesOwned
		10000     | 0
	}

}