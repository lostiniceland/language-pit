package bikes.infrastructure.persistence;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;

@Singleton
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@Startup
public class FlywayMigrationBikes {

	@Resource(lookup = "jdbc/bikes")
	DataSource dataSource;

	@PostConstruct
	public void init(){
		if (dataSource == null) {
			throw new EJBException("no datasource found to execute the db migrations!");
		}
		Flyway flyway = Flyway.configure().dataSource(dataSource).locations("classpath:db/migration/bikes").load();

		flyway.migrate();
	}
}
