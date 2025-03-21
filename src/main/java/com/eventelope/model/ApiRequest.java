package com.eventelope.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an API request with HTTP method, endpoint, headers, and body.
 */
public class ApiRequest {
    private String method;
    private String endpoint;
    private Map<String, String> headers = new HashMap<>();
    private Object body;
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

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
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
                ", hasBody=" + (body != null) +
                ", user='" + user + '\'' +
                '}';
    }
}
