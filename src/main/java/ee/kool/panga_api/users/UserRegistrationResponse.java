package ee.kool.panga_api.users;

import java.time.Instant;

public class UserRegistrationResponse {

    private String userId;
    private String fullName;
    private String email;
    private String apiKey;
    private Instant createdAt;

    public UserRegistrationResponse(String userId, String fullName, String email, String apiKey, Instant createdAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.apiKey = apiKey;
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getApiKey() {
        return apiKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}