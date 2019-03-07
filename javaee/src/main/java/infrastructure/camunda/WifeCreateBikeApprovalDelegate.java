package infrastructure.camunda;

import application.WifeService;
import javax.inject.Inject;
import javax.inject.Named;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

@Named("wifeCreateBikeApproval")
public class WifeCreateBikeApprovalDelegate implements JavaDelegate {

	@Inject
	WifeService wifeService;

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		long bikeId = (long) execution.getVariable("bikeId");
		float value = (float) execution.getVariable("bikePrice");

		wifeService.createNewApproval(bikeId, value);
	}
}
