package br.com.everton.backendextrato.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "financial_separation_workspace")
public class FinancialSeparationWorkspace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_email", nullable = false, unique = true)
    private String userEmail;

    @Column(name = "cashboxes_json", nullable = false, columnDefinition = "TEXT")
    private String cashboxesJson;

    @Column(name = "entries_json", nullable = false, columnDefinition = "TEXT")
    private String entriesJson;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getCashboxesJson() {
        return cashboxesJson;
    }

    public void setCashboxesJson(String cashboxesJson) {
        this.cashboxesJson = cashboxesJson;
    }

    public String getEntriesJson() {
        return entriesJson;
    }

    public void setEntriesJson(String entriesJson) {
        this.entriesJson = entriesJson;
    }
}
