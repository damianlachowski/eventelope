package com.eventelope.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains the criteria for verifying an API response including status code,
 * headers, and JSONPath assertions.
 */
public class ResponseVerifier {
    private Integer statusCode;
    private Map<String, String> headers = new HashMap<>();
    private List<Map<String, Object>> jsonPathAssertions = new ArrayList<>();

    public ResponseVerifier() {
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public List<Map<String, Object>> getJsonPathAssertions() {
        return jsonPathAssertions;
    }

    public void setJsonPathAssertions(List<Map<String, Object>> jsonPathAssertions) {
        this.jsonPathAssertions = jsonPathAssertions;
    }

    /**
     * Add a JSONPath assertion
     * 
     * @param path The JSONPath expression
     * @param expectedValue The expected value
     */
    public void addJsonPathAssertion(String path, Object expectedValue) {
        Map<String, Object> assertion = new HashMap<>();
        assertion.put("path", path);
        assertion.put("value", expectedValue);
        this.jsonPathAssertions.add(assertion);
    }

    @Override
    public String toString() {
        return "ResponseVerifier{" +
                "statusCode=" + statusCode +
                ", headers=" + headers +
                ", jsonPathAssertions=" + jsonPathAssertions +
                '}';
    }
}
