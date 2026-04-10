package br.com.everton.backendextrato.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "notification_logs")
public class MongoNotificationLogDocument {

    @Id
    private String id;

    @Indexed
    private String userEmail;

    private String title;
    private String body;
    private String url;
    private int totalTargets;
    private int totalDelivered;
    private int totalRemoved;
    private int totalFailed;
    private int webTargets;
    private int webDelivered;
    private int webRemoved;
    private int webFailed;
    private int mobileTargets;
    private int mobileDelivered;
    private int mobileRemoved;
    private int mobileFailed;
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

    public String getBody() {
        return body;
    }

    public String getUrl() {
        return url;
    }

    public int getTotalTargets() {
        return totalTargets;
    }

    public int getTotalDelivered() {
        return totalDelivered;
    }

    public int getTotalRemoved() {
        return totalRemoved;
    }

    public int getTotalFailed() {
        return totalFailed;
    }

    public int getWebTargets() {
        return webTargets;
    }

    public int getWebDelivered() {
        return webDelivered;
    }

    public int getWebRemoved() {
        return webRemoved;
    }

    public int getWebFailed() {
        return webFailed;
    }

    public int getMobileTargets() {
        return mobileTargets;
    }

    public int getMobileDelivered() {
        return mobileDelivered;
    }

    public int getMobileRemoved() {
        return mobileRemoved;
    }

    public int getMobileFailed() {
        return mobileFailed;
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

    public void setBody(String body) {
        this.body = body;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTotalTargets(int totalTargets) {
        this.totalTargets = totalTargets;
    }

    public void setTotalDelivered(int totalDelivered) {
        this.totalDelivered = totalDelivered;
    }

    public void setTotalRemoved(int totalRemoved) {
        this.totalRemoved = totalRemoved;
    }

    public void setTotalFailed(int totalFailed) {
        this.totalFailed = totalFailed;
    }

    public void setWebTargets(int webTargets) {
        this.webTargets = webTargets;
    }

    public void setWebDelivered(int webDelivered) {
        this.webDelivered = webDelivered;
    }

    public void setWebRemoved(int webRemoved) {
        this.webRemoved = webRemoved;
    }

    public void setWebFailed(int webFailed) {
        this.webFailed = webFailed;
    }

    public void setMobileTargets(int mobileTargets) {
        this.mobileTargets = mobileTargets;
    }

    public void setMobileDelivered(int mobileDelivered) {
        this.mobileDelivered = mobileDelivered;
    }

    public void setMobileRemoved(int mobileRemoved) {
        this.mobileRemoved = mobileRemoved;
    }

    public void setMobileFailed(int mobileFailed) {
        this.mobileFailed = mobileFailed;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
