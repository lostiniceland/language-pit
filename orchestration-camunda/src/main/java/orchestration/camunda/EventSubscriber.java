package orchestration.camunda;

import common.infrastructure.protobuf.Events.BikeApprovedMessage;
import common.infrastructure.protobuf.Events.BikeCreatedMessage;
import common.infrastructure.protobuf.Events.BikeRejectedMessage;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/")
@Produces({MediaType.APPLICATION_JSON, "application/x-protobuf"})
@Named
public class EventSubscriber {

  private static final Logger logger = LoggerFactory.getLogger(EventSubscriber.class);

  @Inject
  ProcessEngine engine;

  @POST
  @Path("bikes/created")
  public void created(BikeCreatedMessage message){

    Map<String, Object> attributes = new HashMap<>(3);
    attributes.put("type", "bikeCreated");
    attributes.put("bikeId", message.getBikeId());
    attributes.put("value", message.getValue());
    ProcessInstance instance = engine.getRuntimeService()
        .startProcessInstanceByMessage("EventReceived", attributes);
    logger.info("Process with ID '{}' started", instance.getId());
  }

  @POST
  @Path("approvals/accepted")
  public void created(BikeApprovedMessage message){
    Map<String, Object> attributes = new HashMap<>(3);
    attributes.put("type", "approvalAccepted");
    attributes.put("bikeId", message.getBikeId());
    ProcessInstance instance = engine.getRuntimeService()
        .startProcessInstanceByMessage("EventReceived", attributes);
    logger.info("Process with ID '{}' started", instance.getId());
  }

  @POST
  @Path("approvals/rejected")
  public void created(BikeRejectedMessage message){
    Map<String, Object> attributes = new HashMap<>(3);
    attributes.put("type", "approvalRejected");
    attributes.put("bikeId", message.getBikeId());
    ProcessInstance instance = engine.getRuntimeService()
        .startProcessInstanceByMessage("EventReceived", attributes);
    logger.info("Process with ID '{}' started", instance.getId());
  }
}
