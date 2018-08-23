package orchestration.camunda;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

@RequestScoped
@Named
public class ApprovalPublisher implements JavaDelegate {

  public void doSomething(){
    System.out.println("Hello Approval");
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    System.out.println("Hello Y");
  }
}
