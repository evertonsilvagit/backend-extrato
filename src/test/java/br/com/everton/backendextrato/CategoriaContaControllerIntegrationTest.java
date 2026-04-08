package br.com.everton.backendextrato;

import br.com.everton.backendextrato.auth.AuthTokenFilter;
import br.com.everton.backendextrato.auth.AuthenticatedUser;
import br.com.everton.backendextrato.model.CategoriaConta;
import br.com.everton.backendextrato.model.Conta;
import br.com.everton.backendextrato.repository.CategoriaContaRepository;
import br.com.everton.backendextrato.repository.ContaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:categorias-conta;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.flyway.enabled=false",
                "auth.jwt.secret=test-secret"
        }
)
class CategoriaContaControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CategoriaContaRepository categoriaRepository;

    @Autowired
    private ContaRepository contaRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        contaRepository.deleteAll();
        categoriaRepository.deleteAll();
    }

    @Test
    void shouldCreateCategoryAndUseItWhenCreatingBill() throws Exception {
        String categoryPayload = """
                {
                  "name": "Moradia"
                }
                """;

        mockMvc.perform(post("/api/categorias-conta")
                        .requestAttr(AuthTokenFilter.AUTHENTICATED_USER_ATTRIBUTE, new AuthenticatedUser(1L, "user@example.com", "User"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryPayload))
                .andExpect(status().isCreated())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"name\":\"Moradia\"")));

        String billPayload = """
                {
                  "descricao": "Aluguel",
                  "valor": 2200.00,
                  "diaPagamento": 5,
                  "categoria": "Moradia",
                  "mesesVigencia": [1,2,3],
                  "ordem": 1
                }
                """;

        mockMvc.perform(post("/api/contas")
                        .requestAttr(AuthTokenFilter.AUTHENTICATED_USER_ATTRIBUTE, new AuthenticatedUser(1L, "user@example.com", "User"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(billPayload))
                .andExpect(status().isCreated())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"categoria\":\"Moradia\"")));

        mockMvc.perform(get("/api/contas")
                        .requestAttr(AuthTokenFilter.AUTHENTICATED_USER_ATTRIBUTE, new AuthenticatedUser(1L, "user@example.com", "User")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"descricao\":\"Aluguel\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"categoria\":\"Moradia\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"ordem\":1")));
    }

    @Test
    void shouldUpdateBillIncludingOrder() throws Exception {
        CategoriaConta categoria = new CategoriaConta();
        categoria.setNome("Moradia");
        categoria.setUserEmail("user@example.com");
        categoria = categoriaRepository.save(categoria);

        Conta conta = new Conta();
        conta.setDescricao("Internet");
        conta.setValor(new BigDecimal("120.00"));
        conta.setDiaPagamento(10);
        conta.setCategoria(categoria);
        conta.setUserEmail("user@example.com");
        conta.setMesesVigencia(List.of(1, 2, 3));
        conta.setOrdem(2);
        conta = contaRepository.save(conta);

        String payload = """
                {
                  "descricao": "Internet fibra",
                  "valor": 150.00,
                  "diaPagamento": 12,
                  "categoria": "Moradia",
                  "mesesVigencia": [1,2,3,4],
                  "ordem": 7
                }
                """;

        mockMvc.perform(put("/api/contas/" + conta.getId())
                        .requestAttr(AuthTokenFilter.AUTHENTICATED_USER_ATTRIBUTE, new AuthenticatedUser(1L, "user@example.com", "User"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"descricao\":\"Internet fibra\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"diaPagamento\":12")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"ordem\":1")));
    }

    @Test
    void shouldRejectDuplicateCategoryForSameUser() throws Exception {
        String payload = """
                {
                  "name": "Saúde"
                }
                """;

        mockMvc.perform(post("/api/categorias-conta")
                        .requestAttr(AuthTokenFilter.AUTHENTICATED_USER_ATTRIBUTE, new AuthenticatedUser(1L, "user@example.com", "User"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/categorias-conta")
                        .requestAttr(AuthTokenFilter.AUTHENTICATED_USER_ATTRIBUTE, new AuthenticatedUser(1L, "user@example.com", "User"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Já existe uma categoria de conta com este nome.")));
    }

    @Test
    void shouldAllowSameCategoryNameForDifferentUsers() throws Exception {
        String payload = """
                {
                  "name": "Essencial"
                }
                """;

        mockMvc.perform(post("/api/categorias-conta")
                        .requestAttr(AuthTokenFilter.AUTHENTICATED_USER_ATTRIBUTE, new AuthenticatedUser(1L, "first@example.com", "First"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/categorias-conta")
                        .requestAttr(AuthTokenFilter.AUTHENTICATED_USER_ATTRIBUTE, new AuthenticatedUser(2L, "second@example.com", "Second"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        assertThat(categoriaRepository.findAll()).hasSize(2);
    }

    @Test
    void shouldPreventDeletingCategoryThatIsInUse() throws Exception {
        CategoriaConta categoria = new CategoriaConta();
        categoria.setNome("Transporte");
        categoria.setUserEmail("user@example.com");
        categoria = categoriaRepository.save(categoria);

        Conta conta = new Conta();
        conta.setDescricao("Seguro do carro");
        conta.setValor(new BigDecimal("300.00"));
        conta.setDiaPagamento(10);
        conta.setCategoria(categoria);
        conta.setUserEmail("user@example.com");
        conta.setMesesVigencia(List.of(1, 2, 3));
        conta.setOrdem(1);
        contaRepository.save(conta);

        mockMvc.perform(delete("/api/categorias-conta/" + categoria.getId())
                        .requestAttr(AuthTokenFilter.AUTHENTICATED_USER_ATTRIBUTE, new AuthenticatedUser(1L, "user@example.com", "User")))
                .andExpect(status().isConflict())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Não é possível excluir uma categoria de conta que está em uso.")));
    }
}
