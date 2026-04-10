package br.com.everton.backendextrato.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "import_history")
public class MongoImportHistoryDocument {

    @Id
    private String id;

    @Indexed
    private String userEmail;

    private String importType;
    private String source;
    private int total;
    private int created;
    private int updated;
    private int failed;
    private long durationMs;
    private Map<String, Object> metadata;
    private Instant createdAt;

    public String getId() {
        return id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getImportType() {
        return importType;
    }

    public String getSource() {
        return source;
    }

    public int getTotal() {
        return total;
    }

    public int getCreated() {
        return created;
    }

    public int getUpdated() {
        return updated;
    }

    public int getFailed() {
        return failed;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
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

    public void setImportType(String importType) {
        this.importType = importType;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public void setCreated(int created) {
        this.created = created;
    }

    public void setUpdated(int updated) {
        this.updated = updated;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
