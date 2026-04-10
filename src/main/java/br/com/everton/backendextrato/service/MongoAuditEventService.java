package br.com.everton.backendextrato.service;

import br.com.everton.backendextrato.document.MongoAuditEventDocument;
import br.com.everton.backendextrato.dto.MongoAuditEventRequest;
import br.com.everton.backendextrato.dto.MongoAuditEventResponse;
import br.com.everton.backendextrato.repository.MongoAuditEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class MongoAuditEventService {

    private static final Logger log = LoggerFactory.getLogger(MongoAuditEventService.class);
    private final MongoAuditEventRepository repository;

    public MongoAuditEventService(MongoAuditEventRepository repository) {
        this.repository = repository;
    }

    public MongoAuditEventResponse create(String userEmail, MongoAuditEventRequest request) {
        validate(request);
        MongoAuditEventDocument document = buildDocument(userEmail, request.action(), request.resource(), request.source(), request.payload());
        return toResponse(repository.save(document));
    }

    public void record(String userEmail, String action, String resource, String source, Map<String, Object> payload) {
        try {
            MongoAuditEventDocument document = buildDocument(userEmail, action, resource, source, payload);
            repository.save(document);
        } catch (RuntimeException ex) {
            log.warn("Failed to record audit event", ex);
        }
    }

    public List<MongoAuditEventResponse> list(String userEmail) {
        return repository.findAllByUserEmailIgnoreCaseOrderByCreatedAtDesc(userEmail)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private MongoAuditEventDocument buildDocument(
            String userEmail,
            String action,
            String resource,
            String source,
            Map<String, Object> payload
    ) {
        MongoAuditEventDocument document = new MongoAuditEventDocument();
        document.setUserEmail(userEmail);
        document.setAction(action != null ? action.trim() : null);
        document.setResource(resource != null ? resource.trim() : null);
        document.setSource(source != null ? source.trim() : null);
        document.setPayload(payload == null ? Map.of() : payload);
        document.setCreatedAt(Instant.now());
        return document;
    }

    private MongoAuditEventResponse toResponse(MongoAuditEventDocument document) {
        return new MongoAuditEventResponse(
                document.getId(),
                document.getUserEmail(),
                document.getAction(),
                document.getResource(),
                document.getSource(),
                document.getPayload(),
                document.getCreatedAt()
        );
    }

    private void validate(MongoAuditEventRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("payload obrigatorio");
        }
        if (request.action() == null || request.action().isBlank()) {
            throw new IllegalArgumentException("action e obrigatorio");
        }
        if (request.resource() == null || request.resource().isBlank()) {
            throw new IllegalArgumentException("resource e obrigatorio");
        }
    }
}
