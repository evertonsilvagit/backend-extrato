package br.com.everton.backendextrato.config;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;

@Configuration
public class FlywayInitializer {

    private static final Logger logger = LoggerFactory.getLogger(FlywayInitializer.class);

    @Autowired
    private DataSource dataSource;

    @Bean(initMethod = "migrate")
    @DependsOn("dataSource")
    public Flyway flyway() {
        logger.info("Configurando Flyway...");
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .validateOnMigrate(false)
                .load();
        logger.info("Flyway configurado. Executando migrações...");
        return flyway;
    }
}

