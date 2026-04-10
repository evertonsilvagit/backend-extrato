package br.com.everton.backendextrato.service;

import br.com.everton.backendextrato.document.MongoImportHistoryDocument;
import br.com.everton.backendextrato.dto.MongoImportHistoryResponse;
import br.com.everton.backendextrato.repository.MongoImportHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class MongoImportHistoryService {

    private static final Logger log = LoggerFactory.getLogger(MongoImportHistoryService.class);
    private final MongoImportHistoryRepository repository;

    public MongoImportHistoryService(MongoImportHistoryRepository repository) {
        this.repository = repository;
    }

    public void record(
            String userEmail,
            String importType,
            String source,
            int total,
            int created,
            int updated,
            int failed,
            long durationMs,
            Map<String, Object> metadata
    ) {
        try {
            MongoImportHistoryDocument document = new MongoImportHistoryDocument();
            document.setUserEmail(userEmail);
            document.setImportType(importType);
            document.setSource(source);
            document.setTotal(total);
            document.setCreated(created);
            document.setUpdated(updated);
            document.setFailed(failed);
            document.setDurationMs(durationMs);
            document.setMetadata(metadata == null ? Map.of() : metadata);
            document.setCreatedAt(Instant.now());
            repository.save(document);
        } catch (RuntimeException ex) {
            log.warn("Failed to record import history", ex);
        }
    }

    public List<MongoImportHistoryResponse> list(String userEmail) {
        return repository.findAllByUserEmailIgnoreCaseOrderByCreatedAtDesc(userEmail)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private MongoImportHistoryResponse toResponse(MongoImportHistoryDocument document) {
        return new MongoImportHistoryResponse(
                document.getId(),
                document.getUserEmail(),
                document.getImportType(),
                document.getSource(),
                document.getTotal(),
                document.getCreated(),
                document.getUpdated(),
                document.getFailed(),
                document.getDurationMs(),
                document.getMetadata(),
                document.getCreatedAt()
        );
    }
}
