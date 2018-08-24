package orchestration.camunda.delegates;

import com.google.protobuf.GeneratedMessageV3;
import common.infrastructure.protobuf.Events.BikeCreatedMessage;
import common.infrastructure.protobuf.Events.EventsEnvelope;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import orchestration.camunda.ProtobufBinaryMessageBodyReaderWriter;
import orchestration.camunda.ProtobufJsonMessageBodyReaderWriter;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.Incident;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wife.infrastructure.protobuf.Wife.CreateBikeApprovalMessage;

@RequestScoped
@Named
public class WifePublisher implements JavaDelegate {

  private static final Logger logger = LoggerFactory.getLogger(WifePublisher.class);

  @Inject
  JaxRsPublisher jaxRsPublisher;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    EventsEnvelope envelope = (EventsEnvelope) execution.getVariable("envelope");

    if(envelope.hasBikeCreated()){
      BikeCreatedMessage bikeCreated = envelope.getBikeCreated();
      CreateBikeApprovalMessage message = CreateBikeApprovalMessage.newBuilder()
          .setBikeId(bikeCreated.getBikeId())
          .setValue(bikeCreated.getValue())
          .build();
      Optional<Response> response = jaxRsPublisher.sendToWifeService(() -> message);
      if (response.isPresent() && response.get().getStatus() == Status.CREATED.getStatusCode()) {
        logger.info("Event '{}' successfully delivered", message);
      } else {
        logger.error("Failure delivering event '{}'", message);
        execution.createIncident("ServiceDeliveryFailure", null, "Could handle BikeCreated with WifeService");
      }
    }

  }


}
