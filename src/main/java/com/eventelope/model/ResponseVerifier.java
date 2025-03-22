package com.eventelope.model;

import com.eventelope.extraction.ExtractionDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains criteria for verifying API responses.
 */
public class ResponseVerifier {
    private Integer statusCode;
    private Map<String, String> headers = new HashMap<>();
    private List<Map<String, Object>> jsonPathAssertions = new ArrayList<>();
    private List<ExtractionDefinition> extractions = new ArrayList<>();

    /**
     * Get the expected HTTP status code.
     *
     * @return The expected status code
     */
    public Integer getStatusCode() {
        return statusCode;
    }

    /**
     * Set the expected HTTP status code.
     *
     * @param statusCode The expected status code
     */
    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Get the expected HTTP response headers.
     *
     * @return Map of header names to expected values
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Set the expected HTTP response headers.
     *
     * @param headers Map of header names to expected values
     */
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers != null ? headers : new HashMap<>();
    }

    /**
     * Get the JSONPath assertions to verify in the response.
     *
     * @return List of JSONPath assertions
     */
    public List<Map<String, Object>> getJsonPathAssertions() {
        return jsonPathAssertions;
    }

    /**
     * Set the JSONPath assertions to verify in the response.
     *
     * @param jsonPathAssertions List of JSONPath assertions
     */
    public void setJsonPathAssertions(List<Map<String, Object>> jsonPathAssertions) {
        this.jsonPathAssertions = jsonPathAssertions != null ? jsonPathAssertions : new ArrayList<>();
    }

    /**
     * Get the value extractions to perform on the response.
     *
     * @return List of extraction definitions
     */
    public List<ExtractionDefinition> getExtractions() {
        return extractions;
    }

    /**
     * Set the value extractions to perform on the response.
     *
     * @param extractions List of extraction definitions
     */
    public void setExtractions(List<ExtractionDefinition> extractions) {
        this.extractions = extractions != null ? extractions : new ArrayList<>();
    }

    /**
     * Add a single extraction definition.
     *
     * @param extraction The extraction definition to add
     */
    public void addExtraction(ExtractionDefinition extraction) {
        if (extraction != null) {
            this.extractions.add(extraction);
        }
    }

    /**
     * Check if this verifier has any assertions or validations.
     *
     * @return true if there are any validations, false otherwise
     */
    public boolean hasValidations() {
        return statusCode != null 
                || !headers.isEmpty() 
                || !jsonPathAssertions.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ResponseVerifier{");
        
        if (statusCode != null) {
            sb.append("statusCode=").append(statusCode);
        }
        
        if (!headers.isEmpty()) {
            if (statusCode != null) sb.append(", ");
            sb.append("headers=").append(headers);
        }
        
        if (!jsonPathAssertions.isEmpty()) {
            if (statusCode != null || !headers.isEmpty()) sb.append(", ");
            sb.append("jsonPathAssertions=").append(jsonPathAssertions.size());
        }
        
        if (!extractions.isEmpty()) {
            if (statusCode != null || !headers.isEmpty() || !jsonPathAssertions.isEmpty()) sb.append(", ");
            sb.append("extractions=").append(extractions.size());
        }
        
        sb.append('}');
        return sb.toString();
    }
}