package br.com.everton.backendextrato;

import br.com.everton.backendextrato.dto.CreateEntradaRequest;
import br.com.everton.backendextrato.dto.EntradaDto;
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
class EntradasIntegrationTest {

    @LocalServerPort
    int port;

    private WebTestClient client() {
        return WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Test
    @DisplayName("POST /api/entradas cria e retorna 201 com id")
    void criaEntrada() {
        CreateEntradaRequest req = new CreateEntradaRequest(
                "Sioux",
                "PJ",
                new BigDecimal("15000"),
                new BigDecimal("6"),
                List.of(1,2,3,4,5,6,7,8,9,10,11,12)
        );

        EntradaDto dto = client().post()
                .uri("/api/entradas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(EntradaDto.class)
                .returnResult().getResponseBody();

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isNotNull();
        assertThat(dto.nome()).isEqualTo("Sioux");
    }

    @Test
    @DisplayName("GET /api/entradas lista paginada")
    void listaEntradas() {
        // cria duas entradas
        client().post().uri("/api/entradas").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CreateEntradaRequest("A", "PJ", new BigDecimal("100"), new BigDecimal("6"), List.of(1)))
                .exchange().expectStatus().isCreated();
        client().post().uri("/api/entradas").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CreateEntradaRequest("B", "CLT", new BigDecimal("200"), new BigDecimal("6"), List.of(2,3)))
                .exchange().expectStatus().isCreated();

        client().get()
                .uri(uriBuilder -> uriBuilder.path("/api/entradas").queryParam("page", 0).queryParam("size", 10).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").value(v -> assertThat(((Integer) v).intValue()).isGreaterThanOrEqualTo(2));
    }

    @Test
    @DisplayName("GET /api/entradas/{id} 200 e 404")
    void getPorId() {
        EntradaDto criado = client().post().uri("/api/entradas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CreateEntradaRequest("C", "PJ", new BigDecimal("300"), new BigDecimal("6"), List.of(4,5)))
                .exchange().expectStatus().isCreated().expectBody(EntradaDto.class)
                .returnResult().getResponseBody();

        client().get().uri("/api/entradas/" + criado.id())
                .exchange().expectStatus().isOk();

        client().get().uri("/api/entradas/999999")
                .exchange().expectStatus().isNotFound();
    }

    @Test
    @DisplayName("DELETE /api/entradas/{id} remove e retorna 204; inexistente 404")
    void deleteEntrada() {
        // cria
        EntradaDto criado = client().post().uri("/api/entradas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CreateEntradaRequest("Del", "PJ", new BigDecimal("10"), new BigDecimal("6"), List.of(1,2)))
                .exchange().expectStatus().isCreated().expectBody(EntradaDto.class)
                .returnResult().getResponseBody();

        // remove
        client().delete().uri("/api/entradas/" + criado.id())
                .exchange().expectStatus().isNoContent();

        // buscar após deletar -> 404
        client().get().uri("/api/entradas/" + criado.id())
                .exchange().expectStatus().isNotFound();

        // deletar inexistente -> 404
        client().delete().uri("/api/entradas/999999")
                .exchange().expectStatus().isNotFound();
    }
    
    @Test
    @DisplayName("POST /api/entradas validações retornam 400")
    void validacoes() {
        // nome vazio
        client().post().uri("/api/entradas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CreateEntradaRequest(" ", "PJ", new BigDecimal("100"), new BigDecimal("6"), List.of(1)))
                .exchange().expectStatus().isBadRequest();

        // valor <= 0
        client().post().uri("/api/entradas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CreateEntradaRequest("Ok", "PJ", new BigDecimal("0"), new BigDecimal("6"), List.of(1)))
                .exchange().expectStatus().isBadRequest();

        // meses inválidos
        client().post().uri("/api/entradas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CreateEntradaRequest("Ok", "PJ", new BigDecimal("10"), new BigDecimal("6"), List.of()))
                .exchange().expectStatus().isBadRequest();
    }
}
