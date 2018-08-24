package orchestration.camunda.delegates;

import common.infrastructure.protobuf.Events.EventsEnvelope;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
@Named
public class BikePublisher implements JavaDelegate {

  private static final Logger logger = LoggerFactory.getLogger(BikePublisher.class);

  @Resource(lookup = "urlBikesService")
  private String url;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    EventsEnvelope envelope = (EventsEnvelope) execution.getVariable("envelope");
  }
}
