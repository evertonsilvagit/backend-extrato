package br.com.everton.backendextrato;

import br.com.everton.backendextrato.dto.ContaDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ContasIntegrationTest {

    @LocalServerPort
    int port;

    private WebTestClient client() {
        return WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Test
    @DisplayName("POST /api/contas cria e retorna 201 com id")
    void criaConta() {
        ContaDto req = new ContaDto(
                null,
                "Aluguel",
                new BigDecimal("1500"),
                5,
                List.of(1,2,3,4,5,6,7,8,9,10,11,12)
        );

        ContaDto dto = client().post()
                .uri("/api/contas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ContaDto.class)
                .returnResult().getResponseBody();

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isNotNull();
        assertThat(dto.descricao()).isEqualTo("Aluguel");
        assertThat(dto.mesesVigencia()).containsExactlyElementsOf(List.of(1,2,3,4,5,6,7,8,9,10,11,12));
    }

    @Test
    @DisplayName("GET /api/contas lista todas")
    void listaContas() {
        client().post().uri("/api/contas").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ContaDto(null, "Conta A", new BigDecimal("100"), 10, List.of(1)))
                .exchange().expectStatus().isCreated();

        client().get()
                .uri("/api/contas")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").value(v -> assertThat(((Integer) v).intValue()).isGreaterThanOrEqualTo(1));
    }

    @Test
    @DisplayName("DELETE /api/contas/{id} remove e retorna 204")
    void deleteConta() {
        ContaDto criado = client().post().uri("/api/contas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ContaDto(null, "Temp", new BigDecimal("10"), 15, List.of(1)))
                .exchange().expectStatus().isCreated().expectBody(ContaDto.class)
                .returnResult().getResponseBody();

        client().delete().uri("/api/contas/" + criado.id())
                .exchange().expectStatus().isNoContent();

        client().get().uri("/api/contas/" + criado.id())
                .exchange().expectStatus().isNotFound();
    }
}
