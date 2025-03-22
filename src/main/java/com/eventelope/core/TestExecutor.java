package com.eventelope.core;

import com.eventelope.assertion.AssertionProcessor;
import com.eventelope.context.TestContext;
import com.eventelope.extraction.ResponseExtractor;
import com.eventelope.http.RestClient;
import com.eventelope.model.ApiRequest;
import com.eventelope.model.ResponseVerifier;
import com.eventelope.model.Step;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Executes test cases and returns results.
 * Updated to handle the new structured test format with sections for preconditions, setup, execution, and cleanup.
 */
public class TestExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestExecutor.class);
    private final RestClient restClient;
    private final AssertionProcessor assertionProcessor;
    private final ResponseExtractor responseExtractor;

    public TestExecutor() {
        this.restClient = new RestClient();
        this.assertionProcessor = new AssertionProcessor();
        this.responseExtractor = new ResponseExtractor();
    }

    /**
     * Execute a single test case and return the result.
     *
     * @param testCase The test case to execute
     * @return The test result
     */
    public TestResult executeTest(TestCase testCase) {
        LOGGER.info("Executing test: {}", testCase.getName());

        TestResult result = new TestResult(testCase);
        result.setPassed(true); // Assume passed until a failure occurs
        
        // Create a test context for sharing variables between steps
        TestContext testContext = new TestContext();
        
        try {
            // Execute preconditions if they exist
            if (!testCase.getPreconditions().isEmpty()) {
                LOGGER.info("Executing preconditions for test: {}", testCase.getName());
                boolean preconditionsPass = executeSteps(testCase.getPreconditions(), result, testContext);
                if (!preconditionsPass) {
                    LOGGER.error("Preconditions failed for test: {}", testCase.getName());
                    result.setPassed(false);
                    return result;
                }
            }
            
            // Execute setup steps if they exist
            if (!testCase.getSetup().isEmpty()) {
                LOGGER.info("Executing setup for test: {}", testCase.getName());
                boolean setupPass = executeSteps(testCase.getSetup(), result, testContext);
                if (!setupPass) {
                    LOGGER.error("Setup failed for test: {}", testCase.getName());
                    result.setPassed(false);
                    return result;
                }
            }
            
            // Execute main execution steps (mandatory)
            LOGGER.info("Executing main steps for test: {}", testCase.getName());
            boolean executionPass = executeSteps(testCase.getExecution(), result, testContext);
            if (!executionPass) {
                LOGGER.error("Execution failed for test: {}", testCase.getName());
                result.setPassed(false);
            }
            
            // Execute cleanup steps if they exist (always run, even if previous steps failed)
            if (!testCase.getCleanup().isEmpty()) {
                LOGGER.info("Executing cleanup for test: {}", testCase.getName());
                boolean cleanupPass = executeSteps(testCase.getCleanup(), result, testContext);
                if (!cleanupPass) {
                    LOGGER.warn("Cleanup had issues for test: {}", testCase.getName());
                    // Don't fail the test just because cleanup had issues
                }
            }
            
            if (result.isPassed()) {
                LOGGER.info("Test passed: {}", testCase.getName());
            } else {
                LOGGER.info("Test failed: {} - Failures: {}", 
                    testCase.getName(), 
                    String.join(", ", result.getFailureMessages()));
            }
            
            // Store variables from test context in result for reporting
            result.setVariables(testContext.getAllVariables());
            
        } catch (Exception e) {
            result.setPassed(false);
            result.addFailureMessage("Exception occurred: " + e.getMessage());
            LOGGER.error("Error executing test: " + testCase.getName(), e);
        }

        return result;
    }
    
    /**
     * Execute a list of steps and return whether all steps passed.
     *
     * @param steps List of steps to execute
     * @param result The test result to update
     * @param context The test context for storing and retrieving variables
     * @return true if all steps passed, false otherwise
     */
    private boolean executeSteps(List<Step> steps, TestResult result, TestContext context) {
        boolean allPassed = true;
        
        for (Step step : steps) {
            LOGGER.info("Executing step: {}", step.getName());
            
            try {
                ApiRequest request = step.getRequest();
                if (request == null) {
                    LOGGER.warn("Step '{}' has no request defined, skipping", step.getName());
                    continue;
                }
                
                // Apply default headers
                request.applyDefaultHeaders();
                
                // Execute the request with the test context for variable substitution
                Response response = restClient.executeRequest(request, context);
                String responseBody = response.getBody().asString();
                
                // Store the most recent response in the result
                result.setResponse(response);
                result.setResponseBody(responseBody);
                result.setResponseHeaders(response.getHeaders().asList());
                result.setStatusCode(response.getStatusCode());
                
                // Process assertions
                ResponseVerifier verifier = step.getVerify();
                if (verifier != null) {
                    // First verify assertions with variable substitution via context
                    List<String> assertionFailures = assertionProcessor.verifyResponse(response, verifier, context);
                    
                    // If assertions pass, perform extractions
                    if (assertionFailures.isEmpty()) {
                        // Extract values from response and store them in the context
                        if (verifier.getExtractions() != null && !verifier.getExtractions().isEmpty()) {
                            LOGGER.debug("Performing extractions for step: {}", step.getName());
                            responseExtractor.extractAndStoreValues(responseBody, verifier.getExtractions(), context);
                        }
                        
                        LOGGER.info("Step '{}' passed", step.getName());
                        // Track successfully executed steps
                        result.addExecutedStep(step.getName());
                    } else {
                        allPassed = false;
                        for (String failure : assertionFailures) {
                            result.addFailureMessage(String.format("Step '%s': %s", step.getName(), failure));
                        }
                        LOGGER.error("Step '{}' failed with {} assertion failures", 
                            step.getName(), assertionFailures.size());
                    }
                } else {
                    LOGGER.warn("Step '{}' has no verification criteria defined", step.getName());
                    // Still track steps that were executed without verification
                    result.addExecutedStep(step.getName() + " (no verification)");
                }
                
            } catch (Exception e) {
                allPassed = false;
                result.addFailureMessage(String.format("Step '%s': Exception - %s", 
                    step.getName(), e.getMessage()));
                LOGGER.error("Error executing step: " + step.getName(), e);
            }
        }
        
        return allPassed;
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
