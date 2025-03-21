package com.eventelope.assertion;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Handles JSONPath assertions on response bodies.
 */
public class JsonPathAssertion {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonPathAssertion.class);

    /**
     * Assert that a JSONPath expression in a response body matches an expected value.
     *
     * @param responseBody The JSON response body as a string
     * @param jsonPath The JSONPath expression
     * @param expectedValue The expected value
     * @return null if the assertion passes, otherwise an error message
     */
    public String assertJsonPath(String responseBody, String jsonPath, Object expectedValue) {
        try {
            Object actualValue = JsonPath.read(responseBody, jsonPath);
            
            if (!objectEquals(actualValue, expectedValue)) {
                return String.format("JSONPath assertion failed for '%s': expected '%s' but got '%s'",
                        jsonPath, expectedValue, actualValue);
            }
            
            return null; // Assertion passed
        } catch (PathNotFoundException e) {
            return String.format("JSONPath not found: '%s'", jsonPath);
        } catch (Exception e) {
            LOGGER.error("Error evaluating JSONPath: {}", jsonPath, e);
            return String.format("Error evaluating JSONPath '%s': %s", jsonPath, e.getMessage());
        }
    }

    /**
     * Check if expected and actual values are equal, handling different types.
     *
     * @param actual The actual value
     * @param expected The expected value
     * @return true if values are equal
     */
    private boolean objectEquals(Object actual, Object expected) {
        if (actual == null && expected == null) {
            return true;
        }
        
        if (actual == null || expected == null) {
            return false;
        }
        
        // Handle numeric comparisons
        if (actual instanceof Number && expected instanceof Number) {
            if (actual instanceof Double || actual instanceof Float || 
                expected instanceof Double || expected instanceof Float) {
                double actualDouble = ((Number) actual).doubleValue();
                double expectedDouble = ((Number) expected).doubleValue();
                return Math.abs(actualDouble - expectedDouble) < 0.0001;
            } else {
                long actualLong = ((Number) actual).longValue();
                long expectedLong = ((Number) expected).longValue();
                return actualLong == expectedLong;
            }
        }
        
        // Handle booleans
        if (actual instanceof Boolean && expected instanceof Boolean) {
            return Objects.equals(actual, expected);
        }
        
        // Handle strings and other objects
        return Objects.equals(actual.toString(), expected.toString());
    }
}
