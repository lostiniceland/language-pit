package orchestration.camunda;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;

@Startup
@Singleton
public class ProcessEngineStartup {

  private static ProcessEngineConfiguration configuration = new StandalonePersistentH2ProcessEngineConfiguration();

  private ProcessEngine processEngine;
  private RuntimeContainerDelegate runtimeContainerDelegate;

  @PostConstruct
  public void init(){
    processEngine = configuration.buildProcessEngine();
    runtimeContainerDelegate = RuntimeContainerDelegate.INSTANCE.get();
    runtimeContainerDelegate.registerProcessEngine(processEngine);
  }

  @PreDestroy
  public void destroy(){
    processEngine.close();
    runtimeContainerDelegate.unregisterProcessEngine(processEngine);
  }


  private final static class StandalonePersistentH2ProcessEngineConfiguration extends StandaloneInMemProcessEngineConfiguration {

    public StandalonePersistentH2ProcessEngineConfiguration() {
      this.databaseSchemaUpdate = DB_SCHEMA_UPDATE_TRUE;
      this.jdbcUrl = "jdbc:h2:file:/tmp/camunda.db";
//      this.databaseSchemaUpdate = DB_SCHEMA_UPDATE_CREATE_DROP;
//      this.jdbcDriver = null;
//      this.jdbcUrl = null;
//      this.dataSourceJndiName = "jdbc/h2Persistent";
    }
  }

}
