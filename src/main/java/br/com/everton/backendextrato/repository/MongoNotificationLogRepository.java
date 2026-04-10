package br.com.everton.backendextrato.repository;

import br.com.everton.backendextrato.document.MongoNotificationLogDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MongoNotificationLogRepository extends MongoRepository<MongoNotificationLogDocument, String> {
    List<MongoNotificationLogDocument> findAllByUserEmailIgnoreCaseOrderByCreatedAtDesc(String userEmail);
}
