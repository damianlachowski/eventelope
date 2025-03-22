package com.eventelope.assertion;

import com.eventelope.context.TestContext;
import com.eventelope.model.ResponseVerifier;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        return verifyResponse(response, verifier, null);
    }
    
    /**
     * Verify a response against all assertions in a ResponseVerifier with variable substitution.
     *
     * @param response The HTTP response
     * @param verifier The response verifier with assertions
     * @param context The test context containing variables for substitution
     * @return List of failure messages (empty if all assertions pass)
     */
    public List<String> verifyResponse(Response response, ResponseVerifier verifier, TestContext context) {
        List<String> failures = new ArrayList<>();
        
        // Verify status code
        verifyStatusCode(response, verifier, failures);
        
        // Verify headers
        verifyHeaders(response, verifier, failures, context);
        
        // Verify JSONPath assertions
        verifyJsonPathAssertions(response, verifier, failures, context);
        
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
        verifyHeaders(response, verifier, failures, null);
    }
    
    /**
     * Verify the HTTP response headers with variable substitution.
     *
     * @param response The HTTP response
     * @param verifier The response verifier
     * @param failures List to add failure messages to
     * @param context The test context for variable substitution
     */
    private void verifyHeaders(Response response, ResponseVerifier verifier, List<String> failures, TestContext context) {
        if (verifier.getHeaders() != null && !verifier.getHeaders().isEmpty()) {
            for (Map.Entry<String, String> expectedHeader : verifier.getHeaders().entrySet()) {
                String headerName = expectedHeader.getKey();
                String expectedValue = expectedHeader.getValue();
                
                // Replace variables in expected value if context is provided
                if (context != null && expectedValue != null && expectedValue.contains("${")) {
                    expectedValue = replaceVariables(expectedValue, context);
                }
                
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
        verifyJsonPathAssertions(response, verifier, failures, null);
    }
    
    /**
     * Verify JSONPath assertions on the response body with variable substitution.
     *
     * @param response The HTTP response
     * @param verifier The response verifier
     * @param failures List to add failure messages to
     * @param context The test context for variable substitution
     */
    private void verifyJsonPathAssertions(Response response, ResponseVerifier verifier, List<String> failures, TestContext context) {
        if (verifier.getJsonPathAssertions() != null && !verifier.getJsonPathAssertions().isEmpty()) {
            String responseBody = response.getBody().asString();
            LOGGER.debug("Verifying JSON path assertions on response body: {}", responseBody);
            
            for (Map<String, Object> assertion : verifier.getJsonPathAssertions()) {
                String path = (String) assertion.get("path");
                
                // Support both 'value' and 'expected' keys for compatibility
                Object expectedValue = assertion.containsKey("expected") ? 
                    assertion.get("expected") : assertion.get("value");
                
                // Replace variables in expected value if context is provided
                if (context != null && expectedValue != null && expectedValue instanceof String && 
                    ((String)expectedValue).contains("${")) {
                    expectedValue = replaceVariables((String)expectedValue, context);
                    LOGGER.debug("After variable replacement, expected value: {}", expectedValue);
                }
                
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
    
    /**
     * Replace variables in the format ${variableName} with their values from the context.
     * 
     * @param input String containing variables to replace
     * @param context TestContext containing variable values
     * @return String with variables replaced by their values
     */
    private String replaceVariables(String input, TestContext context) {
        if (input == null || context == null) {
            return input;
        }
        
        // Pattern to match ${variable} syntax
        Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(input);
        
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object variableValue = context.getVariable(variableName);
            
            if (variableValue != null) {
                // Replace the variable with its value from context
                matcher.appendReplacement(result, variableValue.toString().replace("$", "\\$"));
                LOGGER.debug("Replaced variable '{}' with value '{}'", variableName, variableValue);
            } else {
                LOGGER.warn("Variable '{}' not found in context, leaving as is", variableName);
                // If variable not found, leave the placeholder as is
                matcher.appendReplacement(result, "\\${" + variableName + "}");
            }
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
}
