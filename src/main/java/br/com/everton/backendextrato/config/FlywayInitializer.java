package br.com.everton.backendextrato.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;

@Configuration
public class FlywayInitializer {

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void migrate() {
        System.out.println("[DEBUG_LOG] FlywayInitializer: Inciando migração manual...");
        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load()
                .migrate();
        System.out.println("[DEBUG_LOG] FlywayInitializer: Migração concluída!");
    }
}
