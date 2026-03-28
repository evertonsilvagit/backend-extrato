package br.com.everton.backendextrato.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "user_data_access",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_data_access_owner_viewer", columnNames = {"owner_email", "viewer_email"})
        }
)
public class UserDataAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_email", nullable = false)
    private String ownerEmail;

    @Column(name = "viewer_email", nullable = false)
    private String viewerEmail;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public String getOwnerEmail() { return ownerEmail; }
    public String getViewerEmail() { return viewerEmail; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }
    public void setViewerEmail(String viewerEmail) { this.viewerEmail = viewerEmail; }
}
