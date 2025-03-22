package com.eventelope.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an API request with HTTP method, endpoint, headers, payload, and timeout settings.
 */
public class ApiRequest {
    private String method;
    private String endpoint;
    private Map<String, String> headers = new HashMap<>();
    private String payload; // Raw JSON string payload
    private String user; // Reference to a user in the users.yaml config
    private Integer timeout; // Socket timeout in milliseconds
    private Integer connectionTimeout; // Connection timeout in milliseconds

    public ApiRequest() {
        this.timeout = 0; // Use default timeout from RestClient
        this.connectionTimeout = 0; // Use default connectionTimeout from RestClient
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
    
    /**
     * Get the socket timeout in milliseconds.
     * 
     * @return The socket timeout in milliseconds, or 0 if not set (which means use the default)
     */
    public Integer getTimeout() {
        return timeout;
    }

    /**
     * Set the socket timeout in milliseconds.
     * 
     * @param timeout The socket timeout in milliseconds
     */
    public void setTimeout(Integer timeout) {
        this.timeout = timeout != null ? timeout : 0;
    }

    /**
     * Get the connection timeout in milliseconds.
     * 
     * @return The connection timeout in milliseconds, or 0 if not set (which means use the default)
     */
    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Set the connection timeout in milliseconds.
     * 
     * @param connectionTimeout The connection timeout in milliseconds
     */
    public void setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout != null ? connectionTimeout : 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ApiRequest{");
        sb.append("method='").append(method).append('\'');
        sb.append(", endpoint='").append(endpoint).append('\'');
        sb.append(", headers=").append(headers);
        sb.append(", hasPayload=").append(payload != null);
        
        if (user != null) {
            sb.append(", user='").append(user).append('\'');
        }
        
        if (timeout > 0) {
            sb.append(", timeout=").append(timeout).append("ms");
        }
        
        if (connectionTimeout > 0) {
            sb.append(", connectionTimeout=").append(connectionTimeout).append("ms");
        }
        
        sb.append('}');
        return sb.toString();
    }
}
