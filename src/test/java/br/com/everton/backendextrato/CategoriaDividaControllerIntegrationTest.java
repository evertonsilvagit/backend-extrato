package br.com.everton.backendextrato;

import br.com.everton.backendextrato.auth.AuthTokenFilter;
import br.com.everton.backendextrato.auth.AuthenticatedUser;
import br.com.everton.backendextrato.model.CategoriaDivida;
import br.com.everton.backendextrato.model.Divida;
import br.com.everton.backendextrato.repository.CategoriaDividaRepository;
import br.com.everton.backendextrato.repository.DividaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:categorias-divida;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.flyway.enabled=false",
                "auth.jwt.secret=test-secret"
        }
)
class CategoriaDividaControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CategoriaDividaRepository categoriaRepository;

    @Autowired
    private DividaRepository dividaRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        dividaRepository.deleteAll();
        categoriaRepository.deleteAll();
    }

    @Test
    void shouldCreateCategoryAndUseItWhenCreatingDebt() throws Exception {
        String categoryPayload = """
                {
                  "name": "Cartão"
                }
                """;

        mockMvc.perform(post("/api/categorias-divida")
                        .requestAttr(
                                AuthTokenFilter.AUTHENTICATED_USER_ATTRIBUTE,
                                new AuthenticatedUser(1L, "user@example.com", "User")
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryPayload))
                .andExpect(status().isCreated())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"name\":\"Cartão\"")));

        String debtPayload = """
                {
                  "description": "Parcelamento notebook",
                  "amount": 3200.50,
                  "category": "Cartão"
                }
                """;

        mockMvc.perform(post("/api/dividas")
                        .requestAttr(
                                AuthTokenFilter.AUTHENTICATED_USER_ATTRIBUTE,
                                new AuthenticatedUser(1L, "user@example.com", "User")
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(debtPayload))
                .andExpect(status().isCreated())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"category\":\"Cartão\"")));

        mockMvc.perform(get("/api/dividas")
                        .requestAttr(
                                AuthTokenFilter.AUTHENTICATED_USER_ATTRIBUTE,
                                new AuthenticatedUser(1L, "user@example.com", "User")
                        ))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"description\":\"Parcelamento notebook\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"category\":\"Cartão\"")));
    }

    @Test
    void shouldRejectDuplicateCategoryForSameUser() throws Exception {
        String payload = """
                {
                  "name": "Moradia"
                }
                """;

        mockMvc.perform(post("/api/categorias-divida")
                        .requestAttr(
                                AuthTokenFilter.AUTHENTICATED_USER_ATTRIBUTE,
                                new AuthenticatedUser(1L, "user@example.com", "User")
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/categorias-divida")
                        .requestAttr(
                                AuthTokenFilter.AUTHENTICATED_USER_ATTRIBUTE,
                                new AuthenticatedUser(1L, "user@example.com", "User")
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Já existe uma categoria de dívida com este nome.")));
    }

    @Test
    void shouldAllowSameCategoryNameForDifferentUsers() throws Exception {
        String payload = """
                {
                  "name": "Essencial"
                }
                """;

        mockMvc.perform(post("/api/categorias-divida")
                        .requestAttr(
                                AuthTokenFilter.AUTHENTICATED_USER_ATTRIBUTE,
                                new AuthenticatedUser(1L, "first@example.com", "First")
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/categorias-divida")
                        .requestAttr(
                                AuthTokenFilter.AUTHENTICATED_USER_ATTRIBUTE,
                                new AuthenticatedUser(2L, "second@example.com", "Second")
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        assertThat(categoriaRepository.findAll()).hasSize(2);
    }

    @Test
    void shouldPreventDeletingCategoryThatIsInUse() throws Exception {
        CategoriaDivida categoria = new CategoriaDivida();
        categoria.setNome("Estudos");
        categoria.setUserEmail("user@example.com");
        categoria = categoriaRepository.save(categoria);

        Divida divida = new Divida();
        divida.setDescricao("Curso");
        divida.setValor(new BigDecimal("999.90"));
        divida.setCategoria(categoria);
        divida.setUserEmail("user@example.com");
        dividaRepository.save(divida);

        mockMvc.perform(delete("/api/categorias-divida/" + categoria.getId())
                        .requestAttr(
                                AuthTokenFilter.AUTHENTICATED_USER_ATTRIBUTE,
                                new AuthenticatedUser(1L, "user@example.com", "User")
                        ))
                .andExpect(status().isConflict())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Não é possível excluir uma categoria de dívida que está em uso.")));
    }
}
