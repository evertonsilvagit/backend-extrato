package br.com.everton.backendextrato;

import br.com.everton.backendextrato.auth.AuthTokenFilter;
import br.com.everton.backendextrato.auth.AuthenticatedUser;
import br.com.everton.backendextrato.repository.InvoiceRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:invoice-record;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.flyway.enabled=false",
                "auth.jwt.secret=test-secret"
        }
)
class InvoiceRecordControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private InvoiceRecordRepository repository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        repository.deleteAll();
    }

    @Test
    void shouldImportAndListInvoices() throws Exception {
        String payload = """
                [
                  {
                    "sourcePath": "drive/2026/NF117.pdf",
                    "filename": "NF117.pdf",
                    "year": "2026",
                    "monthFolder": "Março",
                    "issueDate": "19/03/2026 08:15:01",
                    "number": "000117",
                    "customerDocument": "00.316.268/0001-37",
                    "customerName": "SIOUX CONSULTING LTDA",
                    "grossAmount": 3400.00,
                    "issAmount": 68.00,
                    "netAmount": 3400.00,
                    "serviceType": "101 - Análise e desenvolvimento de sistemas",
                    "notes": "Serviços prestados no período.",
                    "canceled": false,
                    "relativePath": "2026/NF117.pdf"
                  }
                ]
                """;

        mockMvc.perform(post("/api/notas-fiscais/import")
                        .requestAttr(AuthTokenFilter.AUTHENTICATED_USER_ATTRIBUTE, new AuthenticatedUser(1L, "user@example.com", "User"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"created\":1")));

        mockMvc.perform(get("/api/notas-fiscais")
                        .requestAttr(AuthTokenFilter.AUTHENTICATED_USER_ATTRIBUTE, new AuthenticatedUser(1L, "user@example.com", "User")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"customerName\":\"SIOUX CONSULTING LTDA\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"grossAmount\":3400.00")));
    }

    @Test
    void shouldTreatDuplicateSourcePathInSamePayloadAsUpdate() throws Exception {
        String payload = """
                [
                  {
                    "sourcePath": "drive/2026/NF117.pdf",
                    "filename": "NF117.pdf",
                    "year": "2026",
                    "monthFolder": "MarÃ§o",
                    "issueDate": "19/03/2026 08:15:01",
                    "number": "000117",
                    "customerDocument": "00.316.268/0001-37",
                    "customerName": "CLIENTE ORIGINAL",
                    "grossAmount": 3400.00,
                    "issAmount": 68.00,
                    "netAmount": 3332.00,
                    "serviceType": "101 - AnÃ¡lise e desenvolvimento de sistemas",
                    "notes": "Primeira versÃ£o.",
                    "canceled": false,
                    "relativePath": "2026/NF117.pdf"
                  },
                  {
                    "sourcePath": "drive/2026/NF117.pdf",
                    "filename": "NF117-ajustada.pdf",
                    "year": "2026",
                    "monthFolder": "MarÃ§o",
                    "issueDate": "19/03/2026 08:15:01",
                    "number": "000117",
                    "customerDocument": "00.316.268/0001-37",
                    "customerName": "CLIENTE AJUSTADO",
                    "grossAmount": 3500.00,
                    "issAmount": 70.00,
                    "netAmount": 3430.00,
                    "serviceType": "101 - AnÃ¡lise e desenvolvimento de sistemas",
                    "notes": "Segunda versÃ£o.",
                    "canceled": false,
                    "relativePath": "2026/NF117-ajustada.pdf"
                  }
                ]
                """;

        mockMvc.perform(post("/api/notas-fiscais/import")
                        .requestAttr(AuthTokenFilter.AUTHENTICATED_USER_ATTRIBUTE, new AuthenticatedUser(1L, "user@example.com", "User"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"created\":1")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"updated\":1")));

        mockMvc.perform(get("/api/notas-fiscais")
                        .requestAttr(AuthTokenFilter.AUTHENTICATED_USER_ATTRIBUTE, new AuthenticatedUser(1L, "user@example.com", "User")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"customerName\":\"CLIENTE AJUSTADO\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"filename\":\"NF117-ajustada.pdf\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"grossAmount\":3500.00")));
    }
}
