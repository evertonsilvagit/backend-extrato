package br.com.everton.backendextrato.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "invoice_record", uniqueConstraints = @UniqueConstraint(name = "uk_invoice_record_user_email_source_path", columnNames = {"user_email", "source_path"}))
public class InvoiceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_email", nullable = false)
    private String userEmail;
    @Column(name = "source_path", nullable = false, length = 1024)
    private String sourcePath;
    @Column(nullable = false)
    private String filename;
    @Column(name = "issue_year", length = 8)
    private String issueYear;
    @Column(name = "month_folder", length = 120)
    private String monthFolder;
    @Column(name = "issue_date", length = 64)
    private String issueDate;
    @Column(name = "invoice_number", length = 64)
    private String invoiceNumber;
    @Column(name = "customer_document", length = 64)
    private String customerDocument;
    @Column(name = "customer_name")
    private String customerName;
    @Column(name = "customer_email")
    private String customerEmail;
    @Column(name = "customer_city")
    private String customerCity;
    @Column(name = "gross_amount", precision = 19, scale = 2)
    private BigDecimal grossAmount;
    @Column(name = "iss_amount", precision = 19, scale = 2)
    private BigDecimal issAmount;
    @Column(name = "net_amount", precision = 19, scale = 2)
    private BigDecimal netAmount;
    @Column(name = "service_type")
    private String serviceType;
    @Column(columnDefinition = "TEXT")
    private String notes;
    @Column(nullable = false)
    private boolean canceled;
    @Column(name = "relative_path", length = 1024)
    private String relativePath;
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

    public Long getId() { return id; }
    public String getUserEmail() { return userEmail; }
    public String getSourcePath() { return sourcePath; }
    public String getFilename() { return filename; }
    public String getIssueYear() { return issueYear; }
    public String getMonthFolder() { return monthFolder; }
    public String getIssueDate() { return issueDate; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public String getCustomerDocument() { return customerDocument; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getCustomerCity() { return customerCity; }
    public BigDecimal getGrossAmount() { return grossAmount; }
    public BigDecimal getIssAmount() { return issAmount; }
    public BigDecimal getNetAmount() { return netAmount; }
    public String getServiceType() { return serviceType; }
    public String getNotes() { return notes; }
    public boolean isCanceled() { return canceled; }
    public String getRelativePath() { return relativePath; }

    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public void setSourcePath(String sourcePath) { this.sourcePath = sourcePath; }
    public void setFilename(String filename) { this.filename = filename; }
    public void setIssueYear(String issueYear) { this.issueYear = issueYear; }
    public void setMonthFolder(String monthFolder) { this.monthFolder = monthFolder; }
    public void setIssueDate(String issueDate) { this.issueDate = issueDate; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public void setCustomerDocument(String customerDocument) { this.customerDocument = customerDocument; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public void setCustomerCity(String customerCity) { this.customerCity = customerCity; }
    public void setGrossAmount(BigDecimal grossAmount) { this.grossAmount = grossAmount; }
    public void setIssAmount(BigDecimal issAmount) { this.issAmount = issAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setCanceled(boolean canceled) { this.canceled = canceled; }
    public void setRelativePath(String relativePath) { this.relativePath = relativePath; }
}
