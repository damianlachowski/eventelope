package com.eventelope.core;

import com.eventelope.assertion.AssertionProcessor;
import com.eventelope.http.RestClient;
import com.eventelope.model.ApiRequest;
import com.eventelope.model.ResponseVerifier;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Executes test cases and returns results.
 */
public class TestExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestExecutor.class);
    private final RestClient restClient;
    private final AssertionProcessor assertionProcessor;

    public TestExecutor() {
        this.restClient = new RestClient();
        this.assertionProcessor = new AssertionProcessor();
    }

    /**
     * Execute a single test case and return the result.
     *
     * @param testCase The test case to execute
     * @return The test result
     */
    public TestResult executeTest(TestCase testCase) {
        LOGGER.info("Executing test: {}", testCase.getMetadata().getTestName());

        TestResult result = new TestResult(testCase);
        
        try {
            ApiRequest request = testCase.getRequest();
            Response response = restClient.executeRequest(request);
            
            result.setResponse(response);
            result.setResponseBody(response.getBody().asString());
            result.setResponseHeaders(response.getHeaders().asList());
            result.setStatusCode(response.getStatusCode());
            
            // Process assertions
            ResponseVerifier verifier = testCase.getVerifier();
            List<String> assertionFailures = assertionProcessor.verifyResponse(response, verifier);
            
            if (assertionFailures.isEmpty()) {
                result.setPassed(true);
                LOGGER.info("Test passed: {}", testCase.getMetadata().getTestName());
            } else {
                result.setPassed(false);
                result.setFailureMessages(assertionFailures);
                LOGGER.info("Test failed: {} - Failures: {}", 
                    testCase.getMetadata().getTestName(), 
                    String.join(", ", assertionFailures));
            }
        } catch (Exception e) {
            result.setPassed(false);
            result.setFailureMessages(List.of("Exception occurred: " + e.getMessage()));
            LOGGER.error("Error executing test: " + testCase.getMetadata().getTestName(), e);
        }

        return result;
    }

    /**
     * Execute a list of test cases and return all results.
     *
     * @param testCases List of test cases to execute
     * @return List of test results
     */
    public List<TestResult> executeTests(List<TestCase> testCases) {
        List<TestResult> results = new ArrayList<>();
        
        for (TestCase testCase : testCases) {
            results.add(executeTest(testCase));
        }
        
        return results;
    }
}
