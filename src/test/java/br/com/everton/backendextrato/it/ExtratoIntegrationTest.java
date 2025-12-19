package br.com.everton.backendextrato.it;

import br.com.everton.backendextrato.dto.CreateLancamentoRequest;
import br.com.everton.backendextrato.dto.LancamentoDto;
import br.com.everton.backendextrato.model.Tipo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.time.LocalDate;

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
                .uri("/api/extratos/999999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("POST /api/extratos cria e retorna 201")
    void criaLancamento() {
        CreateLancamentoRequest req = new CreateLancamentoRequest(
                LocalDate.now(),
                "CREDITO",
                new BigDecimal("100.00"),
                "Depósito",
                "Salário",
                1L
        );

        LancamentoDto dto = client().post()
                .uri("/api/extratos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(LancamentoDto.class)
                .returnResult().getResponseBody();

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isNotNull();
        assertThat(dto.descricao()).isEqualTo("Depósito");
        assertThat(dto.tipo()).isEqualTo(Tipo.CREDITO);
    }

    @Test
    @DisplayName("GET /api/extratos lista extrato por contaId")
    void listaExtrato() {
        Long contaId = 2L;
        CreateLancamentoRequest req = new CreateLancamentoRequest(
                LocalDate.now(),
                "DEBITO",
                new BigDecimal("50.00"),
                "Compra",
                "Lazer",
                contaId
        );

        client().post().uri("/api/extratos").bodyValue(req).exchange().expectStatus().isCreated();

        client().get()
                .uri(uriBuilder -> uriBuilder.path("/api/extratos")
                        .queryParam("contaId", contaId)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.itens.length()").value(v -> assertThat(((Integer) v).intValue()).isGreaterThanOrEqualTo(1))
                .jsonPath("$.contaId").isEqualTo(contaId);
    }
}
