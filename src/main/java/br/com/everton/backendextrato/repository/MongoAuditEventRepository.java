package br.com.everton.backendextrato.repository;

import br.com.everton.backendextrato.document.MongoAuditEventDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MongoAuditEventRepository extends MongoRepository<MongoAuditEventDocument, String> {
    List<MongoAuditEventDocument> findAllByUserEmailIgnoreCaseOrderByCreatedAtDesc(String userEmail);
}
