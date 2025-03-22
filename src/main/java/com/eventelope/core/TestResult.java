package com.eventelope.core;

import io.restassured.http.Header;
import io.restassured.response.Response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains the results of a test execution including status, response data, and any failure messages.
 * Updated to support the new structured test format and variable storage.
 */
public class TestResult {
    private TestCase testCase;
    private boolean passed;
    private List<String> failureMessages = new ArrayList<>();
    private int statusCode;
    private String responseBody;
    private List<Header> responseHeaders;
    private Response response;
    private LocalDateTime executionTime;
    private long executionDurationMs;
    // For tracking successfully executed steps
    private List<String> executedSteps = new ArrayList<>();
    // For storing test variables (extracted values)
    private Map<String, Object> variables = new HashMap<>();

    public TestResult(TestCase testCase) {
        this.testCase = testCase;
        this.executionTime = LocalDateTime.now();
    }

    public TestCase getTestCase() {
        return testCase;
    }

    public void setTestCase(TestCase testCase) {
        this.testCase = testCase;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public List<String> getFailureMessages() {
        return failureMessages;
    }

    public void setFailureMessages(List<String> failureMessages) {
        this.failureMessages = failureMessages;
    }

    public void addFailureMessage(String message) {
        this.failureMessages.add(message);
    }

    public void addExecutedStep(String stepName) {
        this.executedSteps.add(stepName);
    }

    public List<String> getExecutedSteps() {
        return executedSteps;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public List<Header> getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(List<Header> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public LocalDateTime getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(LocalDateTime executionTime) {
        this.executionTime = executionTime;
    }

    public long getExecutionDurationMs() {
        return executionDurationMs;
    }

    public void setExecutionDurationMs(long executionDurationMs) {
        this.executionDurationMs = executionDurationMs;
    }

    /**
     * Get the test name from the updated TestCase model
     */
    public String getTestName() {
        return testCase.getName();
    }

    /**
     * Get the test description from the updated TestCase model
     */
    public String getTestDescription() {
        return testCase.getDescription();
    }
    
    /**
     * Get the variables map containing all extracted values
     */
    public Map<String, Object> getVariables() {
        return variables;
    }
    
    /**
     * Set the variables map containing all extracted values
     */
    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }
    
    /**
     * Get a specific variable by name
     */
    public Object getVariable(String name) {
        return variables.get(name);
    }
    
    /**
     * Set a specific variable
     */
    public void setVariable(String name, Object value) {
        variables.put(name, value);
    }

    @Override
    public String toString() {
        return "TestResult{" +
                "testCase=" + testCase.getName() +
                ", passed=" + passed +
                ", statusCode=" + statusCode +
                ", executedSteps=" + executedSteps.size() +
                ", variables=" + variables.size() +
                ", executionTime=" + executionTime +
                "}";
    }
}
