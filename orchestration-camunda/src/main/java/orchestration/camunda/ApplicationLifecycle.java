package orchestration.camunda;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Startup
@Singleton
public class ApplicationLifecycle {

  private static final Logger logger = LoggerFactory.getLogger(ApplicationLifecycle.class);

  @Resource(lookup = "h2DbLocation")
  private String h2DbLocation;

  @Resource
  ManagedExecutorService managedExecutorService;
  @Inject
  Instance<KafkaConsumerTask> taskInstanceKafka;

  private ProcessEngine processEngine;
  private RuntimeContainerDelegate runtimeContainerDelegate;

  @PostConstruct
  public void init() {
    ProcessEngineConfiguration configuration = new StandalonePersistentH2ProcessEngineConfiguration(h2DbLocation);
    processEngine = configuration.buildProcessEngine();
    runtimeContainerDelegate = RuntimeContainerDelegate.INSTANCE.get();
    runtimeContainerDelegate.registerProcessEngine(processEngine);

    managedExecutorService.submit(taskInstanceKafka.get()); // do not shutdown/awaitTermination on destroy (this is handled by the server)
  }

  @PreDestroy
  public void destroy() {
    processEngine.close();
    runtimeContainerDelegate.unregisterProcessEngine(processEngine);
  }



  private final static class StandalonePersistentH2ProcessEngineConfiguration extends StandaloneInMemProcessEngineConfiguration {

    StandalonePersistentH2ProcessEngineConfiguration(String dbLocation) {
      this.databaseSchemaUpdate = DB_SCHEMA_UPDATE_TRUE;
      this.jdbcUrl = "jdbc:h2:file:" + dbLocation;
//      this.databaseSchemaUpdate = DB_SCHEMA_UPDATE_CREATE_DROP;
//      this.jdbcDriver = null;
//      this.jdbcUrl = null;
//      this.dataSourceJndiName = "jdbc/h2Persistent";
    }
  }


}
