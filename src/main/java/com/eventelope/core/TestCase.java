package com.eventelope.core;

import com.eventelope.model.ApiRequest;
import com.eventelope.model.ResponseVerifier;
import com.eventelope.model.TestMetadata;

/**
 * Represents a complete test case including metadata, request, and verification criteria.
 */
public class TestCase {
    private TestMetadata metadata;
    private ApiRequest request;
    private ResponseVerifier verifier;
    private String filePath;

    public TestCase() {
    }

    public TestCase(TestMetadata metadata, ApiRequest request, ResponseVerifier verifier, String filePath) {
        this.metadata = metadata;
        this.request = request;
        this.verifier = verifier;
        this.filePath = filePath;
    }

    public TestMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(TestMetadata metadata) {
        this.metadata = metadata;
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
                "metadata=" + metadata +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
