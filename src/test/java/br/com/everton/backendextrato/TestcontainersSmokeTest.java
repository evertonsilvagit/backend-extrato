package br.com.everton.backendextrato;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@EnabledIfEnvironmentVariable(named = "ENABLE_TESTCONTAINERS", matches = "true")
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class TestcontainersSmokeTest {

    @Test
    void loadsContextWithMySQLContainer() {
        // Se chegou aqui, o contexto subiu com o MySQLContainer
    }
}
