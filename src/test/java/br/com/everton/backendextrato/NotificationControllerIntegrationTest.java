package br.com.everton.backendextrato;

import br.com.everton.backendextrato.auth.AuthTokenFilter;
import br.com.everton.backendextrato.auth.AuthenticatedUser;
import br.com.everton.backendextrato.model.MobilePushSubscription;
import br.com.everton.backendextrato.model.PushSubscription;
import br.com.everton.backendextrato.repository.MobilePushSubscriptionRepository;
import br.com.everton.backendextrato.repository.PushSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:notifications;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.flyway.enabled=false",
                "push.vapid.subject=mailto:test@example.com",
                "push.vapid.public-key=test-public-key",
                "push.vapid.private-key=test-private-key"
        }
)
class NotificationControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private PushSubscriptionRepository repository;

    @Autowired
    private MobilePushSubscriptionRepository mobileRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        repository.deleteAll();
        mobileRepository.deleteAll();
    }

    @Test
    void shouldRegisterAndReregisterSubscriptionWithoutDuplicatingEndpoint() throws Exception {
        String payload = """
                {
                  "endpoint": "https://push.example.com/subscriptions/1",
                  "p256dh": "key-1",
                  "auth": "auth-1",
                  "userEmail": "user@example.com",
                  "userName": "Everton"
                }
                """;

        mockMvc.perform(post("/api/notificacoes/subscriptions")
                        .requestAttr(
                                AuthTokenFilter.AUTHENTICATED_USER_ATTRIBUTE,
                                new AuthenticatedUser(1L, "user@example.com", "Everton")
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"created\":true")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"userEmail\":\"user@example.com\"")));

        String updatedPayload = """
                {
                  "endpoint": "https://push.example.com/subscriptions/1",
                  "p256dh": "key-2",
                  "auth": "auth-2",
                  "userEmail": "updated@example.com",
                  "userName": "Evert"
                }
                """;

        mockMvc.perform(post("/api/notificacoes/subscriptions")
                        .requestAttr(
                                AuthTokenFilter.AUTHENTICATED_USER_ATTRIBUTE,
                                new AuthenticatedUser(1L, "updated@example.com", "Evert")
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedPayload))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"created\":false")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"userEmail\":\"updated@example.com\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"userName\":\"Evert\"")));

        assertThat(repository.count()).isEqualTo(1);
        PushSubscription saved = repository.findAll().get(0);
        assertThat(saved.getP256dh()).isEqualTo("key-2");
        assertThat(saved.getAuth()).isEqualTo("auth-2");
    }

    @Test
    void shouldRemoveSubscriptionIdempotently() throws Exception {
        PushSubscription subscription = new PushSubscription();
        subscription.setEndpoint("https://push.example.com/subscriptions/2");
        subscription.setP256dh("key");
        subscription.setAuth("auth");
        subscription.setUserEmail("user@example.com");
        subscription.setUserName("User");
        repository.save(subscription);

        String payload = """
                {
                  "endpoint": "https://push.example.com/subscriptions/2"
                }
                """;

        mockMvc.perform(delete("/api/notificacoes/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNoContent());

        assertThat(repository.count()).isZero();

        mockMvc.perform(delete("/api/notificacoes/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRegisterAndReregisterMobileSubscriptionWithoutDuplicatingToken() throws Exception {
        String payload = """
                {
                  "expoPushToken": "ExponentPushToken[abc123]",
                  "platform": "android",
                  "deviceName": "Pixel",
                  "appVersion": "1.0.0"
                }
                """;

        mockMvc.perform(post("/api/notificacoes/mobile/subscriptions")
                        .requestAttr(
                                AuthTokenFilter.AUTHENTICATED_USER_ATTRIBUTE,
                                new AuthenticatedUser(1L, "user@example.com", "Everton")
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"created\":true")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"platform\":\"android\"")));

        String updatedPayload = """
                {
                  "expoPushToken": "ExponentPushToken[abc123]",
                  "platform": "android",
                  "deviceName": "Pixel 9",
                  "appVersion": "1.0.1"
                }
                """;

        mockMvc.perform(post("/api/notificacoes/mobile/subscriptions")
                        .requestAttr(
                                AuthTokenFilter.AUTHENTICATED_USER_ATTRIBUTE,
                                new AuthenticatedUser(1L, "updated@example.com", "Evert")
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedPayload))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"created\":false")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"userEmail\":\"updated@example.com\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"deviceName\":\"Pixel 9\"")));

        assertThat(mobileRepository.count()).isEqualTo(1);
        MobilePushSubscription saved = mobileRepository.findAll().get(0);
        assertThat(saved.getAppVersion()).isEqualTo("1.0.1");
        assertThat(saved.getDeviceName()).isEqualTo("Pixel 9");
    }

    @Test
    void shouldRemoveMobileSubscriptionIdempotently() throws Exception {
        MobilePushSubscription subscription = new MobilePushSubscription();
        subscription.setExpoPushToken("ExponentPushToken[remove-me]");
        subscription.setPlatform("android");
        subscription.setUserEmail("user@example.com");
        subscription.setUserName("User");
        mobileRepository.save(subscription);

        String payload = """
                {
                  "expoPushToken": "ExponentPushToken[remove-me]"
                }
                """;

        mockMvc.perform(delete("/api/notificacoes/mobile/subscriptions")
                        .requestAttr(
                                AuthTokenFilter.AUTHENTICATED_USER_ATTRIBUTE,
                                new AuthenticatedUser(1L, "user@example.com", "Everton")
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNoContent());

        assertThat(mobileRepository.count()).isZero();

        mockMvc.perform(delete("/api/notificacoes/mobile/subscriptions")
                        .requestAttr(
                                AuthTokenFilter.AUTHENTICATED_USER_ATTRIBUTE,
                                new AuthenticatedUser(1L, "user@example.com", "Everton")
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNoContent());
    }
}
