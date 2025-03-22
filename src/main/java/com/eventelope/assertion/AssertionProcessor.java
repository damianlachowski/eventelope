package com.eventelope.assertion;

import com.eventelope.model.ResponseVerifier;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Processes all assertions for a test case including status code, headers, and JSON content.
 */
public class AssertionProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AssertionProcessor.class);
    private final JsonPathAssertion jsonPathAssertion = new JsonPathAssertion();

    /**
     * Verify a response against all assertions in a ResponseVerifier.
     *
     * @param response The HTTP response
     * @param verifier The response verifier with assertions
     * @return List of failure messages (empty if all assertions pass)
     */
    public List<String> verifyResponse(Response response, ResponseVerifier verifier) {
        List<String> failures = new ArrayList<>();
        
        // Verify status code
        verifyStatusCode(response, verifier, failures);
        
        // Verify headers
        verifyHeaders(response, verifier, failures);
        
        // Verify JSONPath assertions
        verifyJsonPathAssertions(response, verifier, failures);
        
        return failures;
    }

    /**
     * Verify the HTTP status code.
     *
     * @param response The HTTP response
     * @param verifier The response verifier
     * @param failures List to add failure messages to
     */
    private void verifyStatusCode(Response response, ResponseVerifier verifier, List<String> failures) {
        if (verifier.getStatusCode() != null) {
            int expectedStatusCode = verifier.getStatusCode();
            int actualStatusCode = response.getStatusCode();
            
            if (expectedStatusCode != actualStatusCode) {
                failures.add(String.format("Status code assertion failed: expected %d but got %d",
                        expectedStatusCode, actualStatusCode));
            }
        }
    }

    /**
     * Verify the HTTP response headers.
     *
     * @param response The HTTP response
     * @param verifier The response verifier
     * @param failures List to add failure messages to
     */
    private void verifyHeaders(Response response, ResponseVerifier verifier, List<String> failures) {
        if (verifier.getHeaders() != null && !verifier.getHeaders().isEmpty()) {
            for (Map.Entry<String, String> expectedHeader : verifier.getHeaders().entrySet()) {
                String headerName = expectedHeader.getKey();
                String expectedValue = expectedHeader.getValue();
                String actualValue = response.getHeader(headerName);
                
                if (actualValue == null) {
                    failures.add(String.format("Header assertion failed: header '%s' not found in response",
                            headerName));
                } else if (!actualValue.equals(expectedValue)) {
                    failures.add(String.format("Header assertion failed for '%s': expected '%s' but got '%s'",
                            headerName, expectedValue, actualValue));
                }
            }
        }
    }

    /**
     * Verify JSONPath assertions on the response body.
     *
     * @param response The HTTP response
     * @param verifier The response verifier
     * @param failures List to add failure messages to
     */
    private void verifyJsonPathAssertions(Response response, ResponseVerifier verifier, List<String> failures) {
        if (verifier.getJsonPathAssertions() != null && !verifier.getJsonPathAssertions().isEmpty()) {
            String responseBody = response.getBody().asString();
            LOGGER.debug("Verifying JSON path assertions on response body: {}", responseBody);
            
            for (Map<String, Object> assertion : verifier.getJsonPathAssertions()) {
                String path = (String) assertion.get("path");
                
                // Support both 'value' and 'expected' keys for compatibility
                Object expectedValue = assertion.containsKey("expected") ? 
                    assertion.get("expected") : assertion.get("value");
                
                // Get the assertion type if specified, default to 'equals'
                String assertionType = (String) assertion.getOrDefault("type", JsonPathAssertion.EQUALS);
                
                LOGGER.debug("Verifying assertion - Path: {}, Expected: {}, Type: {}", 
                           path, expectedValue, assertionType);
                
                String failureMessage = jsonPathAssertion.assertJsonPath(responseBody, path, expectedValue, assertionType);
                if (failureMessage != null) {
                    LOGGER.warn("Assertion failed: {}", failureMessage);
                    failures.add(failureMessage);
                } else {
                    LOGGER.debug("Assertion passed for path: {}", path);
                }
            }
        }
    }
}
