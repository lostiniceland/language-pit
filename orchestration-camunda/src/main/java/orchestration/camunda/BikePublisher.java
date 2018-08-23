package orchestration.camunda;

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

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    String type = (String) execution.getVariable("type");
    long bikeId = (long) execution.getVariable("bikeId");
    float value = (float) execution.getVariable("value");

    logger.info("Received {} {} {}", type, bikeId, value);
  }
}
