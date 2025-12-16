package br.com.everton.backendextrato;

import br.com.everton.backendextrato.dto.CreateLancamentoRequest;
import br.com.everton.backendextrato.dto.ExtratoResponse;
import br.com.everton.backendextrato.dto.LancamentoDto;
import br.com.everton.backendextrato.model.Tipo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

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
    @DisplayName("POST /api/extratos cria lançamento e retorna 201 com id")
    void criarLancamento() {
        CreateLancamentoRequest req = new CreateLancamentoRequest(
                LocalDate.now(),
                Tipo.CREDITO.name(),
                new BigDecimal("150.75"),
                "Depósito",
                "SALARIO",
                123L
        );

        LancamentoDto body = client()
                .post()
                .uri(baseUrl() + "/api/extratos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(LancamentoDto.class)
                .returnResult()
                .getResponseBody();

        assertThat(body).isNotNull();
        assertThat(body.id()).isNotNull();
        assertThat(body.valor()).isEqualByComparingTo("150.75");
        assertThat(body.tipo()).isEqualTo(Tipo.CREDITO);
    }

    @Test
    @DisplayName("GET /api/extratos lista itens filtrando por contaId e calcula saldos")
    void listarExtrato() {
        // preparar dados
        client().post().uri(baseUrl() + "/api/extratos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CreateLancamentoRequest(LocalDate.parse("2025-01-05"), Tipo.CREDITO.name(), new BigDecimal("100.00"), "dep 1", "TESTE", 999L))
                .exchange().expectStatus().isCreated();
        client().post().uri(baseUrl() + "/api/extratos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CreateLancamentoRequest(LocalDate.parse("2025-01-10"), Tipo.DEBITO.name(), new BigDecimal("40.00"), "deb 1", "TESTE", 999L))
                .exchange().expectStatus().isCreated();

        String url = baseUrl() + "/api/extratos?contaId={contaId}&de={de}&ate={ate}";
        ExtratoResponse resp = client().get()
                .uri(url, Map.of("contaId", 999, "de", "2025-01-01", "ate", "2025-01-31"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ExtratoResponse.class)
                .returnResult().getResponseBody();

        assertThat(resp).isNotNull();
        assertThat(resp.itens()).hasSize(2);
        assertThat(resp.saldoAnterior()).isNotNull();
        assertThat(resp.saldoAtual()).isNotNull();
        assertThat(resp.saldoAtual()).isEqualByComparingTo("60.00");
    }

    @Test
    @DisplayName("GET /api/extratos/{id} retorna 404 para inexistente e 200 após criação")
    void obterPorId() {
        client().get()
                .uri(baseUrl() + "/api/extratos/999999")
                .exchange()
                .expectStatus().isNotFound();

        LancamentoDto criado = client().post()
                .uri(baseUrl() + "/api/extratos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CreateLancamentoRequest(LocalDate.now(), Tipo.CREDITO.name(), new BigDecimal("10.00"), "dep", "TESTE", 1L))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(LancamentoDto.class)
                .returnResult().getResponseBody();
        Long id = criado.id();

        LancamentoDto ok = client().get()
                .uri(baseUrl() + "/api/extratos/" + id)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LancamentoDto.class)
                .returnResult().getResponseBody();
        assertThat(ok).isNotNull();
        assertThat(ok.id()).isEqualTo(id);
    }

    @Test
    @DisplayName("DELETE /api/extratos/{id} remove e retorna 204; inexistente 404")
    void deletarLancamento() {
        // cria
        LancamentoDto criado = client().post()
                .uri(baseUrl() + "/api/extratos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CreateLancamentoRequest(LocalDate.now(), Tipo.CREDITO.name(), new BigDecimal("10.00"), "dep", "TESTE", 1L))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(LancamentoDto.class)
                .returnResult().getResponseBody();

        // remove
        client().delete()
                .uri(baseUrl() + "/api/extratos/" + criado.id())
                .exchange()
                .expectStatus().isNoContent();

        // buscar após deletar -> 404
        client().get()
                .uri(baseUrl() + "/api/extratos/" + criado.id())
                .exchange()
                .expectStatus().isNotFound();

        // deletar inexistente -> 404
        client().delete()
                .uri(baseUrl() + "/api/extratos/999999")
                .exchange()
                .expectStatus().isNotFound();
    }
}
