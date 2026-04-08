package br.com.everton.backendextrato.service;

import br.com.everton.backendextrato.document.MongoLearningEventDocument;
import br.com.everton.backendextrato.dto.MongoLearningEventRequest;
import br.com.everton.backendextrato.dto.MongoLearningEventResponse;
import br.com.everton.backendextrato.repository.MongoLearningEventRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class MongoLearningEventService {

    private final MongoLearningEventRepository repository;

    public MongoLearningEventService(MongoLearningEventRepository repository) {
        this.repository = repository;
    }

    public MongoLearningEventResponse create(String userEmail, MongoLearningEventRequest request) {
        validate(request);

        MongoLearningEventDocument document = new MongoLearningEventDocument();
        document.setUserEmail(userEmail);
        document.setTitle(request.title().trim());
        document.setType(request.type().trim());
        document.setPayload(request.payload() == null ? Map.of() : request.payload());
        document.setCreatedAt(Instant.now());

        return toResponse(repository.save(document));
    }

    public List<MongoLearningEventResponse> list(String userEmail) {
        return repository.findAllByUserEmailIgnoreCaseOrderByCreatedAtDesc(userEmail).stream()
                .map(this::toResponse)
                .toList();
    }

    private MongoLearningEventResponse toResponse(MongoLearningEventDocument document) {
        return new MongoLearningEventResponse(
                document.getId(),
                document.getUserEmail(),
                document.getTitle(),
                document.getType(),
                document.getPayload(),
                document.getCreatedAt()
        );
    }

    private void validate(MongoLearningEventRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("payload obrigatorio");
        }
        if (request.title() == null || request.title().isBlank()) {
            throw new IllegalArgumentException("title e obrigatorio");
        }
        if (request.type() == null || request.type().isBlank()) {
            throw new IllegalArgumentException("type e obrigatorio");
        }
    }
}
