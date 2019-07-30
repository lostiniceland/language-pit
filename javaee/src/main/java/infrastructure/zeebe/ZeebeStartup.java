package infrastructure.zeebe;

import application.WifeService;
import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.response.DeploymentEvent;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import io.zeebe.client.api.worker.JobWorker;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class ZeebeStartup {

	volatile ZeebeClient client;

	@Inject
	WifeService wifeService;

	private List<JobWorker> registeredWorkers = new ArrayList<>(4);

	@Produces
	@ApplicationScoped
	ZeebeClient getZeebeClient() {
		return client;
	}


	public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
		client = ZeebeClient.newClientBuilder().brokerContactPoint("localhost:26500").build();
		// FIXME only deploy when not already deployed
		DeploymentEvent deploymentEvent = client.newDeployCommand()
				.addResourceFromClasspath("wife_bike2.bpmn")
				.send().join(20, TimeUnit.SECONDS);
		System.out.println("Deployment created with key: " + deploymentEvent.getKey());
		registeredWorkers
				.add(client.newWorker().jobType("wifeCreateBikeApproval").handler(new ZeebeStartup.CreateApproval()).timeout(Duration.ofSeconds(10)).open());
		registeredWorkers.add(client.newWorker().jobType("wifeAcceptBike").handler(new ZeebeStartup.AcceptBike()).timeout(Duration.ofSeconds(10)).open());
		registeredWorkers.add(client.newWorker().jobType("wifeRejectBike").handler(new ZeebeStartup.RejectBike()).timeout(Duration.ofSeconds(10)).open());
		registeredWorkers.add(client.newWorker().jobType("DMN").handler(new ZeebeStartup.Dmn()).timeout(Duration.ofSeconds(10)).open());
	}

	public void destroy(@Observes @Destroyed(ApplicationScoped.class) Object init) {
		registeredWorkers.forEach(JobWorker::close);
		client.close();
	}


	private final class CreateApproval implements JobHandler {

		@Override
		public void handle(JobClient client, ActivatedJob job) {
			ApprovalProcessVariable var = job.getVariablesAsType(ApprovalProcessVariable.class);

			wifeService.createNewApproval(var.bikeId, var.value);

			// TODO Exception-Handling
			client.newCompleteCommand(job.getKey());
		}
	}

	private final class AcceptBike implements JobHandler {

		@Override
		public void handle(JobClient client, ActivatedJob job) {
			long bikeId = (long) job.getVariablesAsMap().get("bikeId");

			wifeService.completeApproval(bikeId, true);

			// TODO Exception-Handling
			client.newCompleteCommand(job.getKey());
		}
	}

	private final class RejectBike implements JobHandler {

		@Override
		public void handle(JobClient client, ActivatedJob job) {
			long bikeId = (long) job.getVariablesAsMap().get("bikeId");

			wifeService.completeApproval(bikeId, false);

			// TODO Exception-Handling
			client.newCompleteCommand(job.getKey());
		}
	}

	private final class Dmn implements JobHandler {

		@Override
		public void handle(JobClient client, ActivatedJob job) {
			client.newCompleteCommand(job.getKey()).variables(Collections.singletonMap("automaticDecision.approved", true)).send();
		}
	}

}
