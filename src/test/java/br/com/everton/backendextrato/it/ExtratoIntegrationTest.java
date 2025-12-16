package br.com.everton.backendextrato.it;

import br.com.everton.backendextrato.dto.LancamentoDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ExtratoIntegrationTest {

    @LocalServerPort
    int port;

    private WebTestClient client() {
        return WebTestClient.bindToServer().baseUrl(baseUrl()).build();
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    @DisplayName("GET /api/extratos/{id} inexistente retorna 404")
    void obterPorIdNotFound() {
        client().get()
                .uri(baseUrl() + "/api/extratos/999999")
                .exchange()
                .expectStatus().isNotFound();
    }
}
