package br.com.everton.backendextrato.repository;

import br.com.everton.backendextrato.document.MongoImportHistoryDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MongoImportHistoryRepository extends MongoRepository<MongoImportHistoryDocument, String> {
    List<MongoImportHistoryDocument> findAllByUserEmailIgnoreCaseOrderByCreatedAtDesc(String userEmail);
}
