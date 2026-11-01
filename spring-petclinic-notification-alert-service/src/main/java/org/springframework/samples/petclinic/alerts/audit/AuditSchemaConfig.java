package org.springframework.samples.petclinic.alerts.audit;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

/**
 * Creates the audit table at start-up. Not done with spring.sql.init: the shared config server
 * sets schema-locations for the HSQLDB services and that wins over this service's application.yml.
 */
@Configuration
class AuditSchemaConfig {

    @Bean
    DataSourceInitializer auditSchemaInitializer(DataSource dataSource) {
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ClassPathResource("db/schema.sql")));
        return initializer;
    }
}
