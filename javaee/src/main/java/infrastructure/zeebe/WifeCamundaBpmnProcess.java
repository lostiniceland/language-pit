package infrastructure.zeebe;

import application.WifeBpmnProcess;
import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.response.WorkflowInstanceEvent;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class WifeCamundaBpmnProcess implements WifeBpmnProcess {

	@Inject
	ZeebeClient client;

	@Override
	public void startApprovalProcessForNewBike(long bikeId, float value, int bikesOwned) {
//		Map<String, Object> vars = new HashMap<>(3);
//		vars.put("bikeId", bikeId);
//		vars.put("bikePrice", value);
//		vars.put("bikesOwned", bikesOwned);

		WorkflowInstanceEvent workflowEvent = client.newCreateInstanceCommand()
				.bpmnProcessId("Wife_Approval_Bike")
				.latestVersion()
				.variables(new ApprovalProcessVariable(bikeId, value, bikesOwned))
				.send()
				.join();
		System.out.println(workflowEvent.getWorkflowInstanceKey());
	}
}
