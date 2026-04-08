package br.com.everton.backendextrato.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "learning_events")
public class MongoLearningEventDocument {

    @Id
    private String id;

    @Indexed
    private String userEmail;

    private String title;
    private String type;
    private Map<String, Object> payload;
    private Instant createdAt;

    public String getId() {
        return id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
