package br.com.everton.backendextrato;

import br.com.everton.backendextrato.auth.AuthTokenFilter;
import br.com.everton.backendextrato.auth.AuthenticatedUser;
import br.com.everton.backendextrato.repository.FinancialSeparationWorkspaceRepository;
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
                "spring.datasource.url=jdbc:h2:mem:financial-separation;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.flyway.enabled=false",
                "auth.jwt.secret=test-secret"
        }
)
class FinancialSeparationWorkspaceControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private FinancialSeparationWorkspaceRepository repository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        repository.deleteAll();
    }

    @Test
    void shouldSaveAndReadFinancialSeparationWorkspace() throws Exception {
        String payload = """
                {
                  "cashboxes": [
                    {
                      "id": "conta-pj",
                      "name": "Conta PJ",
                      "owner": "PJ",
                      "instrument": "Conta",
                      "locked": true
                    }
                  ],
                  "entries": [
                    {
                      "id": "1",
                      "date": "2026-03-31",
                      "description": "Mensalidade software",
                      "amount": 129.90,
                      "cashboxId": "conta-pj",
                      "status": "empresa",
                      "categoryGroup": "Empresa",
                      "category": "Software e assinaturas",
                      "tags": ["comprovante-ok"],
                      "kind": "despesa",
                      "transferNature": null,
                      "transferTargetCashboxId": null,
                      "receiptAttached": true,
                      "notes": "Renovacao mensal"
                    }
                  ]
                }
                """;

        mockMvc.perform(put("/api/pf-pj")
                        .requestAttr(AuthTokenFilter.AUTHENTICATED_USER_ATTRIBUTE, new AuthenticatedUser(1L, "user@example.com", "User"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"description\":\"Mensalidade software\"")));

        mockMvc.perform(get("/api/pf-pj")
                        .requestAttr(AuthTokenFilter.AUTHENTICATED_USER_ATTRIBUTE, new AuthenticatedUser(1L, "user@example.com", "User")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"cashboxes\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"category\":\"Software e assinaturas\"")));
    }
}
