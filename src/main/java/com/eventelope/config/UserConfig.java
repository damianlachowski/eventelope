package com.eventelope.config;

/**
 * Represents a user configuration with authentication details.
 */
public class UserConfig {
    private String id;
    private String authType;
    private String username;
    private String password;
    private String token;
    private String apiKey;
    private String apiKeyHeader;

    public UserConfig() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKeyHeader() {
        return apiKeyHeader;
    }

    public void setApiKeyHeader(String apiKeyHeader) {
        this.apiKeyHeader = apiKeyHeader;
    }

    @Override
    public String toString() {
        return "UserConfig{" +
                "id='" + id + '\'' +
                ", authType='" + authType + '\'' +
                '}';
    }
}
