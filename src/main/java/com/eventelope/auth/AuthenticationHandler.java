package com.eventelope.auth;

import com.eventelope.config.ConfigLoader;
import com.eventelope.config.UserConfig;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Handles authentication for API requests based on user configurations.
 */
public class AuthenticationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationHandler.class);
    private final ConfigLoader configLoader = new ConfigLoader();

    /**
     * Apply authentication to a request based on a user identifier.
     *
     * @param requestSpec The request specification to modify
     * @param userId The user identifier from the users.yaml config
     */
    public void applyAuthentication(RequestSpecification requestSpec, String userId) {
        Map<String, UserConfig> users = configLoader.loadUserConfigs();
        
        if (!users.containsKey(userId)) {
            LOGGER.warn("User '{}' not found in user configurations", userId);
            return;
        }
        
        UserConfig user = users.get(userId);
        String authType = user.getAuthType();
        
        if (authType == null) {
            LOGGER.warn("No auth type specified for user '{}'", userId);
            return;
        }
        
        switch (authType.toLowerCase()) {
            case "basic":
                applyBasicAuth(requestSpec, user);
                break;
            case "bearer":
                applyBearerToken(requestSpec, user);
                break;
            case "apikey":
                applyApiKey(requestSpec, user);
                break;
            case "jwt":
                applyJwtToken(requestSpec, user);
                break;
            default:
                LOGGER.warn("Unsupported auth type '{}' for user '{}'", authType, userId);
        }
    }

    /**
     * Apply Basic Authentication.
     */
    private void applyBasicAuth(RequestSpecification requestSpec, UserConfig user) {
        String username = user.getUsername();
        String password = user.getPassword();
        
        if (username != null && password != null) {
            LOGGER.info("Applying Basic Authentication for user '{}'", user.getId());
            requestSpec.auth().preemptive().basic(username, password);
        } else {
            LOGGER.warn("Missing username or password for Basic Authentication");
        }
    }

    /**
     * Apply Bearer Token Authentication.
     */
    private void applyBearerToken(RequestSpecification requestSpec, UserConfig user) {
        String token = user.getToken();
        
        if (token != null) {
            LOGGER.info("Applying Bearer Token Authentication for user '{}'", user.getId());
            requestSpec.header("Authorization", "Bearer " + token);
        } else {
            LOGGER.warn("Missing token for Bearer Authentication");
        }
    }

    /**
     * Apply JWT Token Authentication.
     */
    private void applyJwtToken(RequestSpecification requestSpec, UserConfig user) {
        String token = user.getToken();
        
        if (token != null) {
            LOGGER.info("Applying JWT Authentication for user '{}'", user.getId());
            requestSpec.header("Authorization", "Bearer " + token);
        } else {
            LOGGER.warn("Missing JWT token for JWT Authentication");
        }
    }

    /**
     * Apply API Key Authentication.
     */
    private void applyApiKey(RequestSpecification requestSpec, UserConfig user) {
        String apiKey = user.getApiKey();
        String apiKeyHeader = user.getApiKeyHeader();
        
        if (apiKey != null && apiKeyHeader != null) {
            LOGGER.info("Applying API Key Authentication for user '{}'", user.getId());
            requestSpec.header(apiKeyHeader, apiKey);
        } else {
            LOGGER.warn("Missing API key or header name for API Key Authentication");
        }
    }
}
