package br.com.everton.backendextrato.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "audit_events")
public class MongoAuditEventDocument {

    @Id
    private String id;

    @Indexed
    private String userEmail;

    private String action;
    private String resource;
    private String source;
    private Map<String, Object> payload;
    private Instant createdAt;

    public String getId() {
        return id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getAction() {
        return action;
    }

    public String getResource() {
        return resource;
    }

    public String getSource() {
        return source;
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

    public void setAction(String action) {
        this.action = action;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
