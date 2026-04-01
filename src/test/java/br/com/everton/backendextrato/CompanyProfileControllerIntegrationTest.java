package br.com.everton.backendextrato;

import br.com.everton.backendextrato.auth.AuthTokenFilter;
import br.com.everton.backendextrato.auth.AuthenticatedUser;
import br.com.everton.backendextrato.repository.CompanyProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:company-profile;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.flyway.enabled=false",
                "auth.jwt.secret=test-secret"
        }
)
class CompanyProfileControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CompanyProfileRepository repository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        repository.deleteAll();
    }

    @Test
    void shouldSaveAndReadCompanyProfile() throws Exception {
        String payload = """
                {
                  "legalName": "Empresa Teste LTDA",
                  "tradeName": "Empresa Teste",
                  "cnpj": "00.000.000/0001-00",
                  "taxRegime": "Simples Nacional",
                  "businessEmail": "financeiro@empresa.com",
                  "invoiceEmail": "financeiro@empresa.com",
                  "pixKey": "financeiro@empresa.com"
                }
                """;

        mockMvc.perform(put("/api/empresa")
                        .requestAttr(AuthTokenFilter.AUTHENTICATED_USER_ATTRIBUTE, new AuthenticatedUser(1L, "user@example.com", "User"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"legalName\":\"Empresa Teste LTDA\"")));

        mockMvc.perform(get("/api/empresa")
                        .requestAttr(AuthTokenFilter.AUTHENTICATED_USER_ATTRIBUTE, new AuthenticatedUser(1L, "user@example.com", "User")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"cnpj\":\"00.000.000/0001-00\"")));
    }
}
