package infrastructure.camunda;

import com.ibm.tx.jta.TransactionManagerFactory;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.cdi.CdiJtaProcessEngineConfiguration;
import org.camunda.bpm.engine.cdi.CdiStandaloneProcessEngineConfiguration;

@Startup
@Singleton
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class CamundaStartupEjb {

	@Resource(lookup = "jdbc/camunda")
	private DataSource dataSource;
	// not working because Liberty is not providing TX-Manager via JNDI (see https://github.com/OpenLiberty/open-liberty/issues/1487)
//	@Resource(lookup = "java:appserver/TransactionManager")
//	private TransactionManager transactionManager;

	private ProcessEngine processEngine;
	private RuntimeContainerDelegate runtimeContainerDelegate;

	@PostConstruct
	public void initCamunda() {
		TransactionManager transactionManager = TransactionManagerFactory.getTransactionManager(); // ibm specific
		ProcessEngineConfiguration configuration = new CustomCdiJtaProcessEngineConfiguration(dataSource, transactionManager);
//		ProcessEngineConfiguration configuration = new CustomCdiStandaloneProcessEngineConfiguration(dataSource);
		processEngine = configuration.buildProcessEngine();
		runtimeContainerDelegate = RuntimeContainerDelegate.INSTANCE.get();
		runtimeContainerDelegate.registerProcessEngine(processEngine);

		processEngine.getRepositoryService()
				.createDeployment()
				.addClasspathResource("wife_bike.bpmn")
				.addClasspathResource("wife_bike.dmn")
				.deploy();
	}

	@PreDestroy
	public void shutdownCamunda() {
		processEngine.close();
		runtimeContainerDelegate.unregisterProcessEngine(processEngine);
	}

	/**
	 * Only works with XADataSource
	 */
	private final static class CustomCdiJtaProcessEngineConfiguration extends CdiJtaProcessEngineConfiguration {

		CustomCdiJtaProcessEngineConfiguration(DataSource dataSource, TransactionManager transactionManager) {
			this.dataSource = dataSource;
			this.transactionManager = transactionManager;
			this.transactionsExternallyManaged = true;
//			this.jpaPersistenceUnitName = "camunda";
//			this.jpaHandleTransaction = true;
			this.databaseSchemaUpdate = DB_SCHEMA_UPDATE_TRUE;
			this.authorizationEnabled = false;
			this.jobExecutorActivate = true;
			this.history = HISTORY_FULL;
		}
	}

	private final static class CustomCdiStandaloneProcessEngineConfiguration extends CdiStandaloneProcessEngineConfiguration {

		CustomCdiStandaloneProcessEngineConfiguration(DataSource dataSource) {
			this.dataSource = dataSource;
//			this.transactionsExternallyManaged = true;
			this.databaseSchemaUpdate = DB_SCHEMA_UPDATE_TRUE;
			this.authorizationEnabled = false;
			this.jobExecutorActivate = true;
			this.history = HISTORY_FULL;
		}
	}
}
