package infrastructure.camunda;

import application.WifeBpmnProcess;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.camunda.bpm.engine.ProcessEngine;

@ApplicationScoped
public class WifeCamundaBpmnProcess implements WifeBpmnProcess {

	public static final String BUSINESS_KEY_BIKE_APPROVAL = "key_bike_approval";

	@Inject
	ProcessEngine processEngine;

	@Override
	public void startApprovalProcessForNewBike(long bikeId, float value, int bikesOwned) {
		Map<String, Object> vars = new HashMap<>(3);
		vars.put("bikeId", bikeId);
		vars.put("bikePrice", value);
		vars.put("bikesOwned", bikesOwned);
		processEngine.getRuntimeService().startProcessInstanceByMessage("Message_Wife_Start_Bike_Process", BUSINESS_KEY_BIKE_APPROVAL, vars);
//		processEngine.getRuntimeService()
//				.createMessageCorrelation("Message_Wife_Start_Bike_Process")
//				.processInstanceBusinessKey(BUSINESS_KEY_BIKE_APPROVAL)
//				.correlateWithResult();
	}
}
