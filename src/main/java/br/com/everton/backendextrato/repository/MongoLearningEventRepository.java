package br.com.everton.backendextrato.repository;

import br.com.everton.backendextrato.document.MongoLearningEventDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MongoLearningEventRepository extends MongoRepository<MongoLearningEventDocument, String> {
    List<MongoLearningEventDocument> findAllByUserEmailIgnoreCaseOrderByCreatedAtDesc(String userEmail);
}
