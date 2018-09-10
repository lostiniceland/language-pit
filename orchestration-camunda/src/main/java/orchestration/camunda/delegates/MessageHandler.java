package orchestration.camunda.delegates;

import common.infrastructure.protobuf.Events.EventsEnvelope;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
@Named
public class MessageHandler implements JavaDelegate {

  private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    EventsEnvelope envelope = (EventsEnvelope) execution.getVariable("envelope");
    logger.debug("Received {} {} {}", envelope.toString());

    if (envelope.hasBikeRejected() || envelope.hasBikeApproved()){
      execution.setVariable("serviceToNotify", "bike");
    } else if (envelope.hasBikeCreated() || envelope.hasBikeDeleted()){
      execution.setVariable("serviceToNotify", "wife");
    } else{
      // for example events which are not relevant to the process (like BikeApprovalCreated for testing)
      execution.setVariable("serviceToNotify", "unknown");
      // TODO handle error case
    }
  }
}
