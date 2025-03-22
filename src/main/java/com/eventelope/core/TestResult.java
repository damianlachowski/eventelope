package com.eventelope.core;

import io.restassured.http.Header;
import io.restassured.response.Response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains the results of a test execution including status, response data, and any failure messages.
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

    @Override
    public String toString() {
        return "TestResult{" +
                "testCase=" + testCase.getTestName() +
                ", passed=" + passed +
                ", statusCode=" + statusCode +
                ", executionTime=" + executionTime +
                "}";
    }
}
