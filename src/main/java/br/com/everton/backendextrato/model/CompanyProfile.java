package br.com.everton.backendextrato.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "company_profile")
public class CompanyProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_email", nullable = false, unique = true)
    private String userEmail;
    @Column(name = "legal_name")
    private String legalName;
    @Column(name = "trade_name")
    private String tradeName;
    @Column(length = 32)
    private String cnpj;
    @Column(name = "tax_regime")
    private String taxRegime;
    private String activity;
    @Column(name = "state_registration")
    private String stateRegistration;
    @Column(name = "municipal_registration")
    private String municipalRegistration;
    @Column(name = "opening_date", length = 32)
    private String openingDate;
    @Column(name = "business_phone", length = 64)
    private String businessPhone;
    @Column(name = "business_email")
    private String businessEmail;
    private String website;
    @Column(name = "contact_name")
    private String contactName;
    @Column(name = "zip_code", length = 32)
    private String zipCode;
    private String street;
    private String number;
    private String complement;
    private String district;
    private String city;
    private String state;
    @Column(name = "invoice_email")
    private String invoiceEmail;
    @Column(name = "pix_key")
    private String pixKey;
    @Column(name = "main_bank")
    private String mainBank;
    @Column(name = "billing_notes", columnDefinition = "TEXT")
    private String billingNotes;
    @Column(name = "accountant_name")
    private String accountantName;
    @Column(name = "accountant_email")
    private String accountantEmail;
    @Column(name = "accountant_phone", length = 64)
    private String accountantPhone;
    @Column(name = "payroll_notes", columnDefinition = "TEXT")
    private String payrollNotes;
    @Column(name = "operation_notes", columnDefinition = "TEXT")
    private String operationNotes;
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
    public String getLegalName() { return legalName; }
    public String getTradeName() { return tradeName; }
    public String getCnpj() { return cnpj; }
    public String getTaxRegime() { return taxRegime; }
    public String getActivity() { return activity; }
    public String getStateRegistration() { return stateRegistration; }
    public String getMunicipalRegistration() { return municipalRegistration; }
    public String getOpeningDate() { return openingDate; }
    public String getBusinessPhone() { return businessPhone; }
    public String getBusinessEmail() { return businessEmail; }
    public String getWebsite() { return website; }
    public String getContactName() { return contactName; }
    public String getZipCode() { return zipCode; }
    public String getStreet() { return street; }
    public String getNumber() { return number; }
    public String getComplement() { return complement; }
    public String getDistrict() { return district; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getInvoiceEmail() { return invoiceEmail; }
    public String getPixKey() { return pixKey; }
    public String getMainBank() { return mainBank; }
    public String getBillingNotes() { return billingNotes; }
    public String getAccountantName() { return accountantName; }
    public String getAccountantEmail() { return accountantEmail; }
    public String getAccountantPhone() { return accountantPhone; }
    public String getPayrollNotes() { return payrollNotes; }
    public String getOperationNotes() { return operationNotes; }

    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public void setLegalName(String legalName) { this.legalName = legalName; }
    public void setTradeName(String tradeName) { this.tradeName = tradeName; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public void setTaxRegime(String taxRegime) { this.taxRegime = taxRegime; }
    public void setActivity(String activity) { this.activity = activity; }
    public void setStateRegistration(String stateRegistration) { this.stateRegistration = stateRegistration; }
    public void setMunicipalRegistration(String municipalRegistration) { this.municipalRegistration = municipalRegistration; }
    public void setOpeningDate(String openingDate) { this.openingDate = openingDate; }
    public void setBusinessPhone(String businessPhone) { this.businessPhone = businessPhone; }
    public void setBusinessEmail(String businessEmail) { this.businessEmail = businessEmail; }
    public void setWebsite(String website) { this.website = website; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    public void setStreet(String street) { this.street = street; }
    public void setNumber(String number) { this.number = number; }
    public void setComplement(String complement) { this.complement = complement; }
    public void setDistrict(String district) { this.district = district; }
    public void setCity(String city) { this.city = city; }
    public void setState(String state) { this.state = state; }
    public void setInvoiceEmail(String invoiceEmail) { this.invoiceEmail = invoiceEmail; }
    public void setPixKey(String pixKey) { this.pixKey = pixKey; }
    public void setMainBank(String mainBank) { this.mainBank = mainBank; }
    public void setBillingNotes(String billingNotes) { this.billingNotes = billingNotes; }
    public void setAccountantName(String accountantName) { this.accountantName = accountantName; }
    public void setAccountantEmail(String accountantEmail) { this.accountantEmail = accountantEmail; }
    public void setAccountantPhone(String accountantPhone) { this.accountantPhone = accountantPhone; }
    public void setPayrollNotes(String payrollNotes) { this.payrollNotes = payrollNotes; }
    public void setOperationNotes(String operationNotes) { this.operationNotes = operationNotes; }
}
