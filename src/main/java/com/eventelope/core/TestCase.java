package com.eventelope.core;

import com.eventelope.model.ApiRequest;
import com.eventelope.model.ResponseVerifier;

/**
 * Represents a complete test case including test name, description, request, and verification criteria.
 */
public class TestCase {
    private String testName;
    private String description;
    private ApiRequest request;
    private ResponseVerifier verifier;
    private String filePath;

    public TestCase() {
    }

    public TestCase(String testName, String description, ApiRequest request, ResponseVerifier verifier, String filePath) {
        this.testName = testName;
        this.description = description;
        this.request = request;
        this.verifier = verifier;
        this.filePath = filePath;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ApiRequest getRequest() {
        return request;
    }

    public void setRequest(ApiRequest request) {
        this.request = request;
    }

    public ResponseVerifier getVerifier() {
        return verifier;
    }

    public void setVerifier(ResponseVerifier verifier) {
        this.verifier = verifier;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public String toString() {
        return "TestCase{" +
                "testName='" + testName + '\'' +
                ", description='" + description + '\'' +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
