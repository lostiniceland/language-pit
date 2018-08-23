package orchestration.camunda;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@ApplicationScoped
public class Startup {

  @Resource
  ManagedExecutorService managedExecutorService;
  @Inject
  Instance<KafkaConsumerTask> taskInstance;


  public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
    managedExecutorService.submit(taskInstance.get());
  }


}
