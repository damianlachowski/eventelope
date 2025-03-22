package com.eventelope.core;

import com.eventelope.assertion.AssertionProcessor;
import com.eventelope.condition.ConditionEvaluator;
import com.eventelope.context.TestContext;
import com.eventelope.context.TestStepVariable;
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
import java.util.Map;

/**
 * Executes test cases and returns results.
 * Updated to handle the new structured test format with sections for preconditions, setup, execution, and cleanup.
 */
public class TestExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestExecutor.class);
    private final RestClient restClient;
    private final AssertionProcessor assertionProcessor;
    private final ResponseExtractor responseExtractor;
    private final ConditionEvaluator conditionEvaluator;

    public TestExecutor() {
        this.restClient = new RestClient();
        this.assertionProcessor = new AssertionProcessor();
        this.responseExtractor = new ResponseExtractor();
        this.conditionEvaluator = new ConditionEvaluator();
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
            
            // Store variable tracking information
            if (!testContext.getAllVariablesWithTracking().isEmpty()) {
                for (Map.Entry<String, TestStepVariable> entry : testContext.getAllVariablesWithTracking().entrySet()) {
                    result.addVariableTracking(entry.getKey(), entry.getValue());
                }
            }
            
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
            // Check if the step has a condition that needs to be evaluated
            if (step.hasCondition()) {
                boolean conditionResult = conditionEvaluator.evaluateCondition(step.getCondition(), context);
                if (!conditionResult) {
                    LOGGER.info("Skipping step '{}' as condition '{}' evaluated to false", 
                        step.getName(), step.getCondition());
                    // Add to executed steps but mark as conditionally skipped
                    result.addExecutedStep(step.getName() + " (conditionally skipped)");
                    continue;
                }
                LOGGER.debug("Condition '{}' evaluated to true, executing step '{}'", 
                    step.getCondition(), step.getName());
            }
            
            LOGGER.info("Executing step: {}", step.getName());
            
            try {
                ApiRequest request = step.getRequest();
                if (request == null) {
                    LOGGER.warn("Step '{}' has no request defined, skipping", step.getName());
                    continue;
                }
                
                // Apply default headers
                request.applyDefaultHeaders();
                
                // Check if retries are configured for this step
                boolean stepPassed = false;
                int retryCount = 0;
                int maxRetries = step.getRetries();
                long retryInterval = step.getRetryInterval();
                List<String> assertionFailures = new ArrayList<>();
                Response response = null;
                String responseBody = "";
                
                // Execute with retry logic if configured
                do {
                    // If this is a retry, log it and sleep for the retry interval
                    if (retryCount > 0) {
                        LOGGER.info("Retrying step '{}' (attempt {}/{})", 
                            step.getName(), retryCount, maxRetries);
                        
                        if (retryInterval > 0) {
                            LOGGER.debug("Waiting {}ms before retry", retryInterval);
                            try {
                                Thread.sleep(retryInterval);
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                LOGGER.warn("Retry wait interrupted", ie);
                            }
                        }
                    }
                    
                    try {
                        // Execute the request with the test context for variable substitution
                        response = restClient.executeRequest(request, context);
                        responseBody = response.getBody().asString();
                        
                        // Store the most recent response in the result
                        result.setResponse(response);
                        result.setResponseBody(responseBody);
                        result.setResponseHeaders(response.getHeaders().asList());
                        result.setStatusCode(response.getStatusCode());
                        
                        // Process assertions
                        ResponseVerifier verifier = step.getVerify();
                        if (verifier != null) {
                            // Verify assertions with variable substitution via context
                            assertionFailures = assertionProcessor.verifyResponse(response, verifier, context);
                            
                            // If assertions pass, mark as successful and break the retry loop
                            if (assertionFailures.isEmpty()) {
                                stepPassed = true;
                                break;
                            } else if (retryCount < maxRetries) {
                                LOGGER.debug("Step '{}' failed assertions on attempt {}, will retry", 
                                    step.getName(), retryCount + 1);
                            }
                        } else {
                            // No verification criteria means the step passes automatically
                            LOGGER.warn("Step '{}' has no verification criteria defined", step.getName());
                            stepPassed = true;
                            break;
                        }
                    } catch (Exception e) {
                        if (retryCount < maxRetries) {
                            LOGGER.debug("Step '{}' failed with exception on attempt {}, will retry: {}", 
                                step.getName(), retryCount + 1, e.getMessage());
                        } else {
                            throw e; // Rethrow the exception on the last attempt
                        }
                    }
                    
                    retryCount++;
                } while (retryCount <= maxRetries && !stepPassed);
                
                // After all retries, process the final result
                if (stepPassed) {
                    ResponseVerifier verifier = step.getVerify();
                    // Extract values from response if there are extractions defined
                    if (verifier != null && verifier.getExtractions() != null && !verifier.getExtractions().isEmpty()) {
                        LOGGER.debug("Performing extractions for step: {}", step.getName());
                        responseExtractor.extractAndStoreValues(responseBody, verifier.getExtractions(), context, step.getName());
                    }
                    
                    String stepInfo = step.getName();
                    if (retryCount > 0) {
                        stepInfo += String.format(" (after %d %s)", 
                            retryCount, retryCount == 1 ? "retry" : "retries");
                    }
                    
                    LOGGER.info("Step '{}' passed", stepInfo);
                    // Track successfully executed steps
                    result.addExecutedStep(stepInfo);
                } else {
                    // Step failed after all retries
                    allPassed = false;
                    String retryInfo = "";
                    if (maxRetries > 0) {
                        retryInfo = String.format(" after %d %s", 
                            maxRetries, maxRetries == 1 ? "retry" : "retries");
                    }
                    
                    if (!assertionFailures.isEmpty()) {
                        for (String failure : assertionFailures) {
                            result.addFailureMessage(String.format("Step '%s': %s%s", 
                                step.getName(), failure, retryInfo));
                        }
                        LOGGER.error("Step '{}' failed with {} assertion failures{}", 
                            step.getName(), assertionFailures.size(), retryInfo);
                    } else {
                        // This case would be hit if there was an exception on every retry
                        result.addFailureMessage(String.format("Step '%s': Failed%s", 
                            step.getName(), retryInfo));
                        LOGGER.error("Step '{}' failed{}", step.getName(), retryInfo);
                    }
                }
                
            } catch (Exception e) {
                allPassed = false;
                String retryInfo = "";
                if (step.hasRetryConfig()) {
                    retryInfo = String.format(" (after %d %s)", 
                        step.getRetries(), step.getRetries() == 1 ? "retry" : "retries");
                }
                
                result.addFailureMessage(String.format("Step '%s': Exception - %s%s", 
                    step.getName(), e.getMessage(), retryInfo));
                LOGGER.error("Error executing step: " + step.getName() + retryInfo, e);
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
