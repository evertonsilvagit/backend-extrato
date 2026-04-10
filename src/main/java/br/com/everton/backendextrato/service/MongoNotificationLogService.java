package br.com.everton.backendextrato.service;

import br.com.everton.backendextrato.document.MongoNotificationLogDocument;
import br.com.everton.backendextrato.dto.MongoNotificationLogResponse;
import br.com.everton.backendextrato.dto.PushNotificationTestResponse;
import br.com.everton.backendextrato.repository.MongoNotificationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class MongoNotificationLogService {

    private static final Logger log = LoggerFactory.getLogger(MongoNotificationLogService.class);
    private final MongoNotificationLogRepository repository;

    public MongoNotificationLogService(MongoNotificationLogRepository repository) {
        this.repository = repository;
    }

    public void record(
            String userEmail,
            String title,
            String body,
            String url,
            PushNotificationTestResponse webResult,
            PushNotificationTestResponse mobileResult
    ) {
        try {
            MongoNotificationLogDocument document = new MongoNotificationLogDocument();
            document.setUserEmail(userEmail);
            document.setTitle(title);
            document.setBody(body);
            document.setUrl(url);
            document.setWebTargets(webResult.targetCount());
            document.setWebDelivered(webResult.deliveredCount());
            document.setWebRemoved(webResult.removedCount());
            document.setWebFailed(webResult.failedCount());
            document.setMobileTargets(mobileResult.targetCount());
            document.setMobileDelivered(mobileResult.deliveredCount());
            document.setMobileRemoved(mobileResult.removedCount());
            document.setMobileFailed(mobileResult.failedCount());
            document.setTotalTargets(webResult.targetCount() + mobileResult.targetCount());
            document.setTotalDelivered(webResult.deliveredCount() + mobileResult.deliveredCount());
            document.setTotalRemoved(webResult.removedCount() + mobileResult.removedCount());
            document.setTotalFailed(webResult.failedCount() + mobileResult.failedCount());
            document.setCreatedAt(Instant.now());
            repository.save(document);
        } catch (RuntimeException ex) {
            log.warn("Failed to record notification log", ex);
        }
    }

    public List<MongoNotificationLogResponse> list(String userEmail) {
        return repository.findAllByUserEmailIgnoreCaseOrderByCreatedAtDesc(userEmail)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private MongoNotificationLogResponse toResponse(MongoNotificationLogDocument document) {
        return new MongoNotificationLogResponse(
                document.getId(),
                document.getUserEmail(),
                document.getTitle(),
                document.getBody(),
                document.getUrl(),
                document.getTotalTargets(),
                document.getTotalDelivered(),
                document.getTotalRemoved(),
                document.getTotalFailed(),
                document.getWebTargets(),
                document.getWebDelivered(),
                document.getWebRemoved(),
                document.getWebFailed(),
                document.getMobileTargets(),
                document.getMobileDelivered(),
                document.getMobileRemoved(),
                document.getMobileFailed(),
                document.getCreatedAt()
        );
    }
}
