package com.eventelope.model;

import com.eventelope.extraction.ExtractionDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains the criteria for verifying an API response including status code,
 * headers, JSONPath assertions, and value extractions.
 */
public class ResponseVerifier {
    private Integer statusCode;
    private Map<String, String> headers = new HashMap<>();
    private List<Map<String, Object>> jsonPathAssertions = new ArrayList<>();
    private List<ExtractionDefinition> extractions = new ArrayList<>();

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
    
    /**
     * Gets the list of extractions to perform on the response.
     * 
     * @return The list of extraction definitions
     */
    public List<ExtractionDefinition> getExtractions() {
        return extractions;
    }

    /**
     * Sets the list of extractions to perform on the response.
     * 
     * @param extractions The list of extraction definitions
     */
    public void setExtractions(List<ExtractionDefinition> extractions) {
        this.extractions = extractions;
    }

    /**
     * Adds an extraction definition to extract a value from the response
     * and store it in the test context.
     * 
     * @param jsonPath The JSONPath expression to extract from
     * @param variableName The name of the variable to store the extracted value to
     */
    public void addExtraction(String jsonPath, String variableName) {
        ExtractionDefinition extraction = new ExtractionDefinition();
        extraction.setFrom(jsonPath);
        extraction.setStoreTo(variableName);
        this.extractions.add(extraction);
    }

    @Override
    public String toString() {
        return "ResponseVerifier{" +
                "statusCode=" + statusCode +
                ", headers=" + headers +
                ", jsonPathAssertions=" + jsonPathAssertions +
                ", extractions=" + extractions +
                '}';
    }
}
