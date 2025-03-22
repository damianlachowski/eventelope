package com.eventelope.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an API request with HTTP method, endpoint, headers, and payload.
 */
public class ApiRequest {
    private String method;
    private String endpoint;
    private Map<String, String> headers = new HashMap<>();
    private String payload; // Raw JSON string payload
    private String user; // Reference to a user in the users.yaml config

    public ApiRequest() {
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method.toUpperCase(); // Normalize HTTP method
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void addHeader(String name, String value) {
        this.headers.put(name, value);
    }

    /**
     * Apply default headers while preserving any user-defined headers
     */
    public void applyDefaultHeaders() {
        if (headers == null) {
            headers = new HashMap<>();
        }
        
        // Always add Accept header (will not override if already exists)
        if (!headers.containsKey("Accept")) {
            headers.put("Accept", "application/json");
        }
        
        // Add Content-Type for POST requests (will not override if already exists)
        if ((method.equalsIgnoreCase("POST") || method.equalsIgnoreCase("PUT") || 
             method.equalsIgnoreCase("PATCH")) && !headers.containsKey("Content-Type")) {
            headers.put("Content-Type", "application/json");
        }
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "ApiRequest{" +
                "method='" + method + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", headers=" + headers +
                ", hasPayload=" + (payload != null) +
                ", user='" + user + '\'' +
                '}';
    }
}
