package infrastructure.camunda

import org.camunda.bpm.engine.delegate.JavaDelegate
import org.camunda.bpm.engine.test.Deployment
import org.camunda.bpm.engine.test.ProcessEngineRule
import org.camunda.bpm.engine.test.mock.Mocks
import org.junit.Rule
import spock.lang.Specification
import spock.lang.Unroll

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
	@Unroll
	def "Process with automatic approval gets #status"() {
		given:
		def vars = [bikePrice: bikePrice, bikesOwned: bikesOwned]

		when: 'approval process gets started'
		def instance = processEngineRule.getRuntimeService().startProcessInstanceByMessage(WifeCamundaBpmnProcess.MESSAGE, WifeCamundaBpmnProcess.BUSINESS_KEY_BIKE_APPROVAL, vars)

		then: 'execution passes the task "Create Approval" as well as the automatic decision'
		assertThat(instance).hasPassed("Task_CreateApproval")
		assertThat(instance).hasPassed("Task_Decide_Bike_Automatically")

		and: 'a #bikePrice bike passes the #status task'
		assertThat(instance).hasPassed(passedTask)

		and: 'process is ended'
		assertThat(instance).isEnded()

		where:
		status     | bikePrice | bikesOwned | passedTask
		'approved' | 1000      | 1          | 'Task_Approve_Bike'
		'rejected' | 10000     | 2          | 'Task_Reject_Bike'
	}


	@Deployment(resources = ["wife_bike.bpmn", "wife_bike.dmn"])
	@Unroll
	def "Process with manual approval gets #status"() {
		given:
		def vars = [bikePrice: bikePrice, bikesOwned: bikesOwned]

		when: 'approval process gets started'
		def instance = processEngineRule.getRuntimeService().startProcessInstanceByMessage(WifeCamundaBpmnProcess.MESSAGE, WifeCamundaBpmnProcess.BUSINESS_KEY_BIKE_APPROVAL, vars)

		then: 'execution passes the task "Create Approval" as well as the automatic decision'
		assertThat(instance).hasPassed("Task_CreateApproval")
		assertThat(instance).hasPassed("Task_Decide_Bike_Automatically")

		and: 'a #bikePrice bike needs manual approval'
		assertThat(instance).isWaitingAt("Task_Decide_Bike_Manually")

		when: 'bike approved manually'
		def task = processEngine().getTaskService().createTaskQuery().taskDefinitionKey("Task_Decide_Bike_Manually").singleResult()
		complete(task, withVariables("manualDecision", manualDecision))

		then: 'bike passes the #status task'
		assertThat(instance).hasPassed(passedTask)

		and: 'process is ended'
		assertThat(instance).isEnded()

		where:
		status     | bikePrice | bikesOwned | manualDecision | passedTask
		'approved' | 5000      | 3          | true           | 'Task_Approve_Bike'
		'rejected' | 10000     | 0          | false          | 'Task_Reject_Bike'
	}
}