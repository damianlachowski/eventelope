package com.eventelope.model;

import com.eventelope.template.TemplateVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an API request configuration.
 */
public class ApiRequest {
    private String url;
    private String endpoint; // Alternative to url for better semantics
    private String method = "GET";
    private Map<String, String> headers = new HashMap<>();
    private String payload;
    private String payloadFile;
    private String contentType = "application/json";
    private String user;
    private Integer timeout = 30000; // Default request timeout in milliseconds
    private Integer connectionTimeout = 10000; // Default connection timeout in milliseconds
    private List<TemplateVariable> templateVariables = new ArrayList<>();

    /**
     * Default constructor.
     */
    public ApiRequest() {
        // Default constructor for deserialization
    }

    /**
     * Get the API endpoint URL.
     *
     * @return The URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Set the API endpoint URL.
     *
     * @param url The URL
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Get the API endpoint URL (synonym for getUrl).
     *
     * @return The endpoint URL
     */
    public String getEndpoint() {
        return endpoint != null ? endpoint : url;
    }

    /**
     * Set the API endpoint URL (synonym for setUrl).
     *
     * @param endpoint The endpoint URL
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Apply default headers like content type if not already specified.
     */
    public void applyDefaultHeaders() {
        if (contentType != null && !headers.containsKey("Content-Type")) {
            headers.put("Content-Type", contentType);
        }
    }

    /**
     * Get the HTTP method.
     *
     * @return The HTTP method (GET, POST, PUT, DELETE, etc.)
     */
    public String getMethod() {
        return method;
    }

    /**
     * Set the HTTP method.
     *
     * @param method The HTTP method (GET, POST, PUT, DELETE, etc.)
     */
    public void setMethod(String method) {
        this.method = method != null ? method.toUpperCase() : "GET";
    }

    /**
     * Get the request headers.
     *
     * @return Map of header names to values
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Set the request headers.
     *
     * @param headers Map of header names to values
     */
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers != null ? headers : new HashMap<>();
    }

    /**
     * Get the inline request payload.
     *
     * @return The payload content
     */
    public String getPayload() {
        return payload;
    }

    /**
     * Set the inline request payload.
     *
     * @param payload The payload content
     */
    public void setPayload(String payload) {
        this.payload = payload;
    }

    /**
     * Get the path to the payload file.
     *
     * @return The payload file path
     */
    public String getPayloadFile() {
        return payloadFile;
    }

    /**
     * Set the path to the payload file.
     *
     * @param payloadFile The payload file path
     */
    public void setPayloadFile(String payloadFile) {
        this.payloadFile = payloadFile;
    }

    /**
     * Get the content type.
     *
     * @return The content type (e.g., "application/json")
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Set the content type.
     *
     * @param contentType The content type (e.g., "application/json")
     */
    public void setContentType(String contentType) {
        this.contentType = contentType != null ? contentType : "application/json";
    }

    /**
     * Get the user for authentication.
     *
     * @return The user identifier
     */
    public String getUser() {
        return user;
    }

    /**
     * Set the user for authentication.
     *
     * @param user The user identifier
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Add a header to the request.
     *
     * @param name The header name
     * @param value The header value
     */
    public void addHeader(String name, String value) {
        if (name != null && value != null) {
            this.headers.put(name, value);
        }
    }

    /**
     * Check if this request has any payload data.
     *
     * @return true if there is a payload, false otherwise
     */
    public boolean hasPayload() {
        return payload != null || payloadFile != null;
    }

    /**
     * Get the request timeout.
     *
     * @return The request timeout in milliseconds
     */
    public Integer getTimeout() {
        return timeout;
    }

    /**
     * Set the request timeout.
     *
     * @param timeout The request timeout in milliseconds
     */
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    /**
     * Get the connection timeout.
     *
     * @return The connection timeout in milliseconds
     */
    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Set the connection timeout.
     *
     * @param connectionTimeout The connection timeout in milliseconds
     */
    public void setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * Get the template variables for this request.
     *
     * @return List of template variables
     */
    public List<TemplateVariable> getTemplateVariables() {
        return templateVariables;
    }

    /**
     * Set the template variables for this request.
     *
     * @param templateVariables List of template variables
     */
    public void setTemplateVariables(List<TemplateVariable> templateVariables) {
        this.templateVariables = templateVariables != null ? templateVariables : new ArrayList<>();
    }

    /**
     * Add a template variable to this request.
     *
     * @param templateVariable The template variable to add
     */
    public void addTemplateVariable(TemplateVariable templateVariable) {
        if (templateVariable != null) {
            this.templateVariables.add(templateVariable);
        }
    }

    @Override
    public String toString() {
        return "ApiRequest{" +
                "method='" + method + '\'' +
                ", url='" + url + '\'' +
                ", contentType='" + contentType + '\'' +
                ", headers=" + headers.size() +
                (payload != null ? ", payload='" + payload.substring(0, Math.min(payload.length(), 50)) + "...'" : "") +
                (payloadFile != null ? ", payloadFile='" + payloadFile + '\'' : "") +
                (user != null ? ", user='" + user + '\'' : "") +
                ", timeout=" + timeout +
                ", connectionTimeout=" + connectionTimeout +
                ", templateVariables=" + templateVariables.size() +
                '}';
    }
}