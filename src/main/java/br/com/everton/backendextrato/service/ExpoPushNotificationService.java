package br.com.everton.backendextrato.service;

import br.com.everton.backendextrato.dto.PushNotificationTestResponse;
import br.com.everton.backendextrato.model.MobilePushSubscription;
import br.com.everton.backendextrato.repository.MobilePushSubscriptionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExpoPushNotificationService {

    private static final Logger log = LoggerFactory.getLogger(ExpoPushNotificationService.class);
    private static final String EXPO_PUSH_API_URL = "https://exp.host/--/api/v2/push/send";
    private static final String DEVICE_NOT_REGISTERED = "DeviceNotRegistered";

    private final MobilePushSubscriptionRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient restClient;

    public ExpoPushNotificationService(MobilePushSubscriptionRepository repository) {
        this.repository = repository;
        this.restClient = RestClient.builder().build();
    }

    public PushNotificationTestResponse sendToUser(String userEmail, String title, String body, String url) {
        List<MobilePushSubscription> targets = repository.findAllByUserEmailIgnoreCase(userEmail.trim());
        if (targets.isEmpty()) {
            return new PushNotificationTestResponse(0, 0, 0, 0);
        }

        List<Map<String, Object>> payload = new ArrayList<>();
        for (MobilePushSubscription subscription : targets) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("to", subscription.getExpoPushToken());
            item.put("title", title);
            item.put("body", body);
            item.put("sound", "default");
            item.put("channelId", "default");
            Map<String, String> data = new LinkedHashMap<>();
            if (url != null && !url.isBlank()) {
                data.put("url", url.trim());
            }
            if (!data.isEmpty()) {
                item.put("data", data);
            }
            payload.add(item);
        }

        try {
            String responseBody = restClient.post()
                    .uri(EXPO_PUSH_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(String.class);

            return summarizeResponse(targets, responseBody);
        } catch (Exception ex) {
            log.warn("Expo push delivery failed for userEmail={} with error={}", sanitize(userEmail), sanitize(ex.getMessage()));
            return new PushNotificationTestResponse(targets.size(), 0, 0, targets.size());
        }
    }

    private PushNotificationTestResponse summarizeResponse(List<MobilePushSubscription> targets, String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody == null ? "{}" : responseBody);
            JsonNode data = root.path("data");

            int delivered = 0;
            int removed = 0;
            int failed = 0;

            for (int index = 0; index < targets.size(); index++) {
                MobilePushSubscription subscription = targets.get(index);
                JsonNode entry = data.isArray() && index < data.size() ? data.get(index) : null;
                String status = entry != null ? entry.path("status").asText("") : "";
                String detailError = entry != null ? entry.path("details").path("error").asText("") : "";

                if ("ok".equalsIgnoreCase(status)) {
                    delivered++;
                    continue;
                }

                if (DEVICE_NOT_REGISTERED.equalsIgnoreCase(detailError)) {
                    repository.delete(subscription);
                    removed++;
                    continue;
                }

                failed++;
            }

            return new PushNotificationTestResponse(targets.size(), delivered, removed, failed);
        } catch (Exception ex) {
            log.warn("Expo push response parsing failed with error={}", sanitize(ex.getMessage()));
            return new PushNotificationTestResponse(targets.size(), 0, 0, targets.size());
        }
    }

    private String sanitize(String value) {
        if (value == null) {
            return "<null>";
        }
        String sanitized = value.replace("\r", " ").replace("\n", " ").trim();
        return sanitized.isEmpty() ? "<empty>" : sanitized;
    }
}
